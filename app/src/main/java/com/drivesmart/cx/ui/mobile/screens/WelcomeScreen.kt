package com.drivesmart.cx.ui.mobile.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel

@Composable
fun WelcomeScreen(
    viewModel: DriveSmartViewModel,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido a\nDriveSmartCX",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Parece que aún no tienes vehículos registrados. Comencemos configurando tu primer vehículo o importa un respaldo existente.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Registrar Mi Primer Vehículo")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { importJsonLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importar Respaldo (JSON)")
        }
    }
}
