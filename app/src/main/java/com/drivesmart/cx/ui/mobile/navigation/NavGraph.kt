package com.drivesmart.cx.ui.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.drivesmart.cx.ui.mobile.screens.*
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel

@Composable
fun DriveSmartNavGraph(
    navController: NavHostController,
    viewModel: DriveSmartViewModel
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    if (vehicles == null) return
    
    val startDestination = if (vehicles!!.isEmpty()) Screen.Welcome.route else Screen.Dashboard.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.VehiculoForm.createRoute()) },
                onNavigateToErrorLogs = { navController.navigate(Screen.ErrorLogs.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToGastos = { navController.navigate(Screen.Gastos.route) },
                onNavigateToServicios = { navController.navigate(Screen.Servicios.route) },
                onNavigateToTramites = { navController.navigate(Screen.Tramites.route) },
                onNavigateToEmergencias = { navController.navigate(Screen.Emergencias.route) },
                onNavigateToBitacora = { navController.navigate(Screen.Bitacora.route) },
                onNavigateToEstacionamiento = { navController.navigate(Screen.Estacionamiento.route) },
                onNavigateToSeguro = { navController.navigate(Screen.Seguro.route) },
                onNavigateToPreventivos = { navController.navigate(Screen.Preventivos.route) },
                onNavigateToInfoVehiculo = { navController.navigate(Screen.InformacionVehiculo.route) },
                onNavigateToGarage = { navController.navigate(Screen.Garage.route) },
                onNavigateToConfig = { navController.navigate(Screen.Configuracion.route) },
                onNavigateToErrorLogs = { navController.navigate(Screen.ErrorLogs.route) },
                onEditVehicle = { id -> navController.navigate(Screen.VehiculoForm.createRoute(id)) }
            )
        }
        composable(Screen.Servicios.route) {
            ServiciosScreen(viewModel)
        }
        composable(Screen.Tramites.route) {
            TramitesScreen(viewModel)
        }
        composable(Screen.Emergencias.route) {
            EmergenciasScreen(
                viewModel = viewModel,
                onNavigateToSOSConfig = { navController.navigate(Screen.SOSConfig.route) }
            )
        }
        composable(Screen.SOSConfig.route) {
            EmergenciasSOSScreen(viewModel)
        }
        composable(Screen.InformacionVehiculo.route) {
            InformacionVehiculoScreen(viewModel)
        }
        composable(Screen.Garage.route) {
            GarageScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.VehiculoForm.createRoute()) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bitacora.route) {
            BitacoraScreen(
                viewModel = viewModel,
                onNavigateToRuta = { viajeId ->
                    navController.navigate(Screen.RutaViaje.createRoute(viajeId))
                }
            )
        }
        composable(
            route = Screen.RutaViaje.route,
            arguments = listOf(navArgument("viajeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val viajeId = backStackEntry.arguments?.getLong("viajeId") ?: -1L
            RutaViajeScreen(
                viajeId = viajeId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Seguro.route) {
            SeguroScreen(viewModel)
        }
        composable(Screen.Preventivos.route) {
            PreventivosScreen(viewModel)
        }
        composable(Screen.Estacionamiento.route) {
            EstacionamientoScreen(viewModel)
        }
        composable(
            route = Screen.VehiculoForm.route,
            arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("vehiculoId") ?: -1L
            VehiculoFormScreen(
                vehiculoId = id,
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Gastos.route) {
            GastosScreen(viewModel)
        }
        composable(Screen.Configuracion.route) {
            ConfiguracionScreen(viewModel)
        }
        composable(Screen.ErrorLogs.route) {
            ErrorLogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
