package com.drivesmart.cx.ui.mobile.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.GastoEntity
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
    
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

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
                    Text(
                        "$${String.format(Locale.getDefault(), "%.2f", totalMes)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        Calendar.getInstance().apply { set(Calendar.MONTH, selectedMonth); set(Calendar.YEAR, selectedYear) }.time
                    )
                    Text(monthName.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredGastos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Sin gastos este mes", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                } else {
                    items(filteredGastos) { gasto ->
                        GastoItem(
                            gasto = gasto,
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
fun GastoItem(gasto: GastoEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                    "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(gasto.fecha))}${if (gasto.litros != null) " - ${gasto.litros} L" else ""} - ${gasto.nota ?: ""}",
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri?.toString()
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
                                modifier = Modifier.fillMaxWidth().height(150.dp).clickable { photoPickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { photoUri = null },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.Red)
                            }
                        }
                    } else {
                        OutlinedButton(onClick = { photoPickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
fun MonthYearPicker(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.DateRange, contentDescription = "Filtrar por fecha")
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Últimos 12 meses
            for (i in 0 until 12) {
                val cal = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, -i)
                }
                val m = cal.get(Calendar.MONTH)
                val y = cal.get(Calendar.YEAR)
                val label = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                
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
