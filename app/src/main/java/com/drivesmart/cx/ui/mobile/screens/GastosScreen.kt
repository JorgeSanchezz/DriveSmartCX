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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.ui.mobile.components.FullScreenImageDialog
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.DateTimeUtils
import com.drivesmart.cx.util.FileHelper
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
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
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
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
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

            if (filteredGastos.isNotEmpty()) {
                GastosChartCard(gastos = filteredGastos)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
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
                onConfirm = { cat, monto, litros, fecha, nota, photoUri ->
                    if (selectedGasto == null) {
                        viewModel.addGasto(cat, monto, litros, nota, photoUri, fecha)
                    } else {
                        viewModel.updateGasto(selectedGasto!!.copy(
                            categoria = cat,
                            monto = monto,
                            litros = litros,
                            fecha = fecha,
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
fun GastosChartCard(gastos: List<GastoEntity>) {
    val total = gastos.sumOf { it.monto }
    val categoryTotals = gastos.groupBy { it.categoria }
        .mapValues { it.value.sumOf { g -> g.monto } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Distribución", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categoryTotals.take(4).forEach { (category, amount) ->
                    val percentage = if (total > 0) (amount / total).toFloat() else 0f
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.weight(1f))
                            Text("$${String.format(Locale.getDefault(), "%.0f", amount)} (${(percentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )
                    }
                }
            }
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
                    modifier = Modifier.size(60.dp).padding(4.dp).align(Alignment.Top).clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Text(gasto.categoria, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${sdf.format(Date(gasto.fecha))}${if (gasto.litros != null) " - ${gasto.litros} L" else ""} ${if (!gasto.nota.isNullOrBlank()) " - ${gasto.nota}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Text(
                    "$${gasto.monto}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddGastoDialog(
    gasto: GastoEntity? = null,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double?, Long, String, String?) -> Unit
) {
    var categoria by remember { mutableStateOf(gasto?.categoria ?: "Gasolina") }
    var monto by remember { mutableStateOf(gasto?.monto?.toString() ?: "") }
    var litros by remember { mutableStateOf(gasto?.litros?.toString() ?: "") }
    var nota by remember { mutableStateOf(gasto?.nota ?: "") }
    var photoUri by remember { mutableStateOf(gasto?.photoUri) }
    var fecha by remember { mutableLongStateOf(gasto?.fecha ?: System.currentTimeMillis()) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val internalPath = FileHelper.copyFileToInternalStorage(context, it, "gastos")
            if (internalPath != null) {
                photoUri = internalPath
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = DateTimeUtils.localToUtc(fecha))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        val calOld = Calendar.getInstance().apply { timeInMillis = fecha }
                        fecha = DateTimeUtils.combineDateUtcAndTimeLocal(
                            it,
                            calOld.get(Calendar.HOUR_OF_DAY),
                            calOld.get(Calendar.MINUTE)
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = fecha }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = false
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calNew = Calendar.getInstance().apply {
                        timeInMillis = fecha
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    fecha = calNew.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gasto == null) "Nuevo Gasto" else "Editar Gasto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Otra categoría...") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = sdf.format(Date(fecha)),
                    onValueChange = { },
                    label = { Text("Fecha y Hora") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Cambiar Fecha")
                            }
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Build, contentDescription = "Cambiar Hora")
                            }
                        }
                    }
                )

                if (categoria == "Gasolina") {
                    OutlinedTextField(
                        value = litros,
                        onValueChange = { litros = it },
                        label = { Text("Litros") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text(if (categoria == "Refacciones") "Descripción de la refacción" else "Nota") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Text("Foto del Recibo", style = MaterialTheme.typography.labelLarge)
                if (photoUri != null) {
                    Box {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(MaterialTheme.shapes.medium)
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
        },
        confirmButton = {
            Button(onClick = { onConfirm(categoria, monto.toDoubleOrNull() ?: 0.0, litros.toDoubleOrNull(), fecha, nota, photoUri) }) {
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
}
