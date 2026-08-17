package com.drivesmart.cx.ui.mobile.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.ui.mobile.components.FullScreenImageDialog
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(viewModel: DriveSmartViewModel) {
    val gastos by viewModel.currentGastos.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGasto by remember { mutableStateOf<GastoEntity?>(null) }
    
    val sdf = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { 
            timeZone = TimeZone.getTimeZone("UTC") 
        } 
    }
    
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    val availableMonths = remember(gastos) {
        gastos.map { gasto ->
            val cal = Calendar.getInstance().apply { timeInMillis = gasto.fecha }
            cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
        }.distinct().sortedByDescending { it.first * 12 + it.second }
    }

    LaunchedEffect(availableMonths) {
        if (availableMonths.isNotEmpty()) {
            val currentPair = selectedYear to selectedMonth
            if (!availableMonths.contains(currentPair)) {
                val (latestYear, latestMonth) = availableMonths.first()
                selectedYear = latestYear
                selectedMonth = latestMonth
            }
        }
    }

    val filteredGastos = remember(gastos, selectedMonth, selectedYear) {
        gastos.filter { gasto ->
            val cal = Calendar.getInstance().apply { timeInMillis = gasto.fecha }
            cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
        }
    }

    val totalMes = remember(filteredGastos) {
        filteredGastos.sumOf { it.monto }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Historial de Gastos") },
                actions = {
                    MonthYearPicker(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        availableMonths = availableMonths,
                        onMonthYearSelected = { m, y ->
                            selectedMonth = m
                            selectedYear = y
                        }
                    )
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedGasto = null
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Gasto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Panel de Resumen Mensual
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen Mensual", style = MaterialTheme.typography.labelMedium)
                    if (availableMonths.isEmpty()) {
                        Text("No hay registros", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text(
                            "$${String.format(Locale.getDefault(), "%.2f", totalMes)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val monthName = remember(selectedMonth, selectedYear) {
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(
                                Calendar.getInstance().apply { 
                                    set(Calendar.YEAR, selectedYear)
                                    set(Calendar.MONTH, selectedMonth) 
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }.time
                            )
                        }
                        Text(monthName.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableMonths.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No se tienen registros", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                } else if (filteredGastos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Sin gastos este mes", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                } else {
                    items(filteredGastos) { gasto ->
                        GastoItem(
                            gasto = gasto,
                            sdf = sdf,
                            onEdit = { selectedGasto = gasto; showAddDialog = true },
                            onDelete = { viewModel.removeGasto(gasto) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddGastoDialog(
                gasto = selectedGasto,
                availableCategories = categories,
                onDismiss = { showAddDialog = false },
                onConfirm = { cat, monto, litros, nota, photoUri ->
                    if (selectedGasto == null) {
                        viewModel.addGasto(cat, monto, litros, nota, photoUri)
                    } else {
                        viewModel.updateGasto(selectedGasto!!.copy(
                            categoria = cat,
                            monto = monto,
                            litros = litros,
                            nota = nota,
                            photoUri = photoUri
                        ))
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun GastoItem(gasto: GastoEntity, sdf: SimpleDateFormat, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (gasto.photoUri != null) {
                AsyncImage(
                    model = gasto.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).padding(4.dp).align(Alignment.Top),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Text(gasto.categoria, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${sdf.format(Date(gasto.fecha))}${if (gasto.litros != null) " - ${gasto.litros} L" else ""} - ${gasto.nota ?: ""}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "$${gasto.monto}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGastoDialog(
    gasto: GastoEntity? = null,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double?, String, String?) -> Unit
) {
    var categoria by remember { mutableStateOf(gasto?.categoria ?: "Gasolina") }
    var monto by remember { mutableStateOf(gasto?.monto?.toString() ?: "") }
    var litros by remember { mutableStateOf(gasto?.litros?.toString() ?: "") }
    var nota by remember { mutableStateOf(gasto?.nota ?: "") }
    var photoUri by remember { mutableStateOf(gasto?.photoUri) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            photoUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gasto == null) "Nuevo Gasto" else "Editar Gasto") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Categoría", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableCategories.forEach { cat ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Otra categoría...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                if (categoria == "Gasolina") {
                    item {
                        OutlinedTextField(
                            value = litros,
                            onValueChange = { litros = it },
                            label = { Text("Litros") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it },
                        label = { Text(if (categoria == "Refacciones") "Descripción de la refacción" else "Nota") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Foto del Recibo", style = MaterialTheme.typography.labelLarge)
                    if (photoUri != null) {
                        Box {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clickable { showFullScreenImage = true },
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", tint = Color.White)
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { photoUri = null },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.Red)
                                }
                            }
                        }
                    } else {
                        OutlinedButton(onClick = { photoPickerLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir Foto")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(categoria, monto.toDoubleOrNull() ?: 0.0, litros.toDoubleOrNull(), nota, photoUri) }) {
                Text(if (gasto == null) "Añadir" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showFullScreenImage && photoUri != null) {
        FullScreenImageDialog(photoUri = photoUri!!, onDismiss = { showFullScreenImage = false })
    }
}

@Composable
fun MonthYearPicker(
    selectedMonth: Int,
    selectedYear: Int,
    availableMonths: List<Pair<Int, Int>>,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.DateRange, contentDescription = "Filtrar por fecha")
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (availableMonths.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin registros") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                availableMonths.forEach { (y, m) ->
                    val cal = Calendar.getInstance().apply { 
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val label = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(cal.time)
                    
                    DropdownMenuItem(
                        text = { Text(label.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onMonthYearSelected(m, y)
                            expanded = false
                        },
                        leadingIcon = {
                            if (m == selectedMonth && y == selectedYear) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }
}
