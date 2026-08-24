package com.drivesmart.cx

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.drivesmart.cx.ui.mobile.navigation.DriveSmartNavGraph
import com.drivesmart.cx.ui.mobile.screens.LockScreen
import com.drivesmart.cx.ui.theme.BrandPrimary
import com.drivesmart.cx.ui.theme.DriveSmartCXTheme
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.BiometricHelper
import com.drivesmart.cx.util.NotificationHelper
import com.drivesmart.cx.util.VehicleBrand
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var biometricHelper: BiometricHelper
    
    private val viewModel: DriveSmartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            val vehicles by viewModel.allVehicles.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
            val isSessionAuth by viewModel.isAuthenticatedSession.collectAsState()
            val appPrimaryColorHex by viewModel.appPrimaryColor.collectAsState()
            var isAuthenticated by remember { mutableStateOf(false) }

            // 1. Esperar a que los vehículos carguen de la DB
            if (vehicles == null) return@setContent 

            val currentVehicle = vehicles!!.find { it.isSelected } ?: vehicles!!.firstOrNull()
            
            val primaryColor = if (appPrimaryColorHex != null) {
                try { Color(android.graphics.Color.parseColor(appPrimaryColorHex)) } catch (e: Exception) { 
                    com.drivesmart.cx.util.AppLogger.error("MainActivity", "Error parsing app color", e)
                    BrandPrimary 
                }
            } else {
                currentVehicle?.let { 
                    if (it.marca == "Otro" && it.customColorHex != null) {
                        try { Color(android.graphics.Color.parseColor(it.customColorHex)) } catch (e: Exception) { 
                            com.drivesmart.cx.util.AppLogger.error("MainActivity", "Error parsing vehicle color", e)
                            VehicleBrand.OTRO.color 
                        }
                    } else {
                        VehicleBrand.fromString(it.marca).color
                    }
                } ?: VehicleBrand.OTRO.color
            }

            DriveSmartCXTheme(primaryColor = primaryColor) {
                Surface {
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { _ -> }

                    LaunchedEffect(Unit) {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        // Opcional: Pedir permiso de SMS para el botón de pánico
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.SEND_SMS)
                        }

                        locationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }

                    if (isAuthenticated || !isBiometricEnabled || vehicles!!.isEmpty() || isSessionAuth) {
                        val navController = rememberNavController()
                        DriveSmartNavGraph(navController = navController, viewModel = viewModel)
                    } else {
                        LockScreen(
                            onAuthenticate = {
                                biometricHelper.showBiometricPrompt(
                                    activity = this@MainActivity,
                                    onSuccess = { isAuthenticated = true },
                                    onError = { }
                                )
                            }
                        )

                        LaunchedEffect(Unit) {
                            biometricHelper.showBiometricPrompt(
                                activity = this@MainActivity,
                                onSuccess = { isAuthenticated = true },
                                onError = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
