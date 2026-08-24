package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.VehicleBrand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoFormScreen(
    vehiculoId: Long,
    viewModel: DriveSmartViewModel,
    onSuccess: () -> Unit
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val existingVehicle = vehicles?.find { it.id == vehiculoId }

    var nombre by remember { mutableStateOf("") }
    var placas by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("Otro") }
    var tipo by remember { mutableStateOf("Coche") }
    var customMarca by remember { mutableStateOf("") }
    var customColor by remember { mutableStateOf("#607D8B") }
    var biometricEnabled by remember { mutableStateOf(true) }

    val isFirstVehicle = vehicles.orEmpty().isEmpty()

    val colors = listOf(
        "#607D8B", // Default
        "#E31837", // Rojo
        "#003478", // Azul
        "#4CAF50", // Verde
        "#FF9800", // Naranja
        "#9C27B0", // Morado
        "#009688", // Teal
        "#000000"  // Negro
    )

    LaunchedEffect(existingVehicle) {
        existingVehicle?.let {
            nombre = it.nombre
            placas = it.placas
            vin = it.vin
            modelo = it.modelo
            anio = it.anio.toString()
            km = it.kilometrajeActual.toString()
            marca = it.marca
            tipo = it.tipo
            customMarca = it.customMarca ?: ""
            customColor = it.customColorHex ?: "#607D8B"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehiculoId == -1L) "Registrar Vehículo" else "Editar Vehículo") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Tipo de Vehículo", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Coche", "Moto").forEach { option ->
                        FilterChip(
                            selected = tipo == option,
                            onClick = { tipo = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            item {
                Text("Marca", style = MaterialTheme.typography.labelLarge)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = marca,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        label = { Text("Seleccionar Marca") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        VehicleBrand.getBrandsForType(tipo).forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.displayName) },
                                onClick = {
                                    marca = brand.displayName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (marca == "Otro") {
                item {
                    OutlinedTextField(
                        value = customMarca,
                        onValueChange = { customMarca = it },
                        label = { Text("Nombre de la Marca") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
                item {
                    Text("Color de la Marca", style = MaterialTheme.typography.bodySmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        items(colors) { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = customColor == hex
                            
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { customColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Personalizado (Ej: Mi Rayo)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)) }
            item { OutlinedTextField(value = placas, onValueChange = { placas = it }, label = { Text("Placas") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)) }
            item { OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("Número de Serie (VIN)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)) }
            item { OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo / Versión") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)) }
            item {
                OutlinedTextField(
                    value = anio,
                    onValueChange = { anio = it },
                    label = { Text("Año") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it },
                    label = { Text("Kilometraje Actual") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            if (isFirstVehicle) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Seguridad", style = MaterialTheme.typography.titleMedium)
                    ListItem(
                        headlineContent = { Text("Autenticación Biométrica") },
                        supportingContent = { Text("Protege tus datos con huella digital") },
                        trailingContent = {
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { biometricEnabled = it }
                            )
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        if (isFirstVehicle) {
                            viewModel.setBiometricEnabled(biometricEnabled)
                            viewModel.setAuthenticated(true)
                        }
                        
                        if (vehiculoId == -1L) {
                            viewModel.addVehicle(nombre, placas, vin, modelo, anio.toIntOrNull() ?: 2024, km.toDoubleOrNull() ?: 0.0, marca, tipo, if (marca == "Otro") customMarca else null, if (marca == "Otro") customColor else null)
                        } else {
                            existingVehicle?.let {
                                viewModel.updateVehicle(
                                    it.copy(
                                        nombre = nombre,
                                        placas = placas,
                                        vin = vin,
                                        modelo = modelo,
                                        anio = anio.toIntOrNull() ?: it.anio,
                                        kilometrajeActual = km.toDoubleOrNull() ?: it.kilometrajeActual,
                                        marca = marca,
                                        tipo = tipo,
                                        customMarca = if (marca == "Otro") customMarca else null,
                                        customColorHex = if (marca == "Otro") customColor else null
                                    )
                                )
                            }
                        }
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nombre.isNotBlank() && placas.isNotBlank()
                ) {
                    Text(if (vehiculoId == -1L) "Guardar Vehículo" else "Actualizar Vehículo")
                }
            }

            if (vehiculoId != -1L) {
                item {
                    OutlinedButton(
                        onClick = {
                            existingVehicle?.let { viewModel.deleteVehicle(it) }
                            onSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar Vehículo")
                    }
                }
            }
        }
    }
}
