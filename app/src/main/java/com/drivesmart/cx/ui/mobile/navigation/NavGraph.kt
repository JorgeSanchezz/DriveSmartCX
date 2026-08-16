package com.drivesmart.cx.ui.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
            com.drivesmart.cx.ui.mobile.screens.WelcomeScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.VehiculoForm.createRoute()) }
            )
        }
        composable(Screen.Dashboard.route) {
            com.drivesmart.cx.ui.mobile.screens.DashboardScreen(
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
                onEditVehicle = { id -> navController.navigate(Screen.VehiculoForm.createRoute(id)) }
            )
        }
        composable(Screen.Servicios.route) {
            com.drivesmart.cx.ui.mobile.screens.ServiciosScreen(viewModel)
        }
        composable(Screen.Tramites.route) {
            com.drivesmart.cx.ui.mobile.screens.TramitesScreen(viewModel)
        }
        composable(Screen.Emergencias.route) {
            com.drivesmart.cx.ui.mobile.screens.EmergenciasScreen(
                viewModel = viewModel,
                onNavigateToSOSConfig = { navController.navigate(Screen.SOSConfig.route) }
            )
        }
        composable(Screen.SOSConfig.route) {
            com.drivesmart.cx.ui.mobile.screens.EmergenciasSOSScreen(viewModel)
        }
        composable(Screen.InformacionVehiculo.route) {
            com.drivesmart.cx.ui.mobile.screens.InformacionVehiculoScreen(viewModel)
        }
        composable(Screen.Garage.route) {
            com.drivesmart.cx.ui.mobile.screens.GarageScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.VehiculoForm.createRoute()) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bitacora.route) {
            com.drivesmart.cx.ui.mobile.screens.BitacoraScreen(viewModel)
        }
        composable(Screen.Seguro.route) {
            com.drivesmart.cx.ui.mobile.screens.SeguroScreen(viewModel)
        }
        composable(Screen.Preventivos.route) {
            com.drivesmart.cx.ui.mobile.screens.PreventivosScreen(viewModel)
        }
        composable(Screen.Estacionamiento.route) {
            com.drivesmart.cx.ui.mobile.screens.EstacionamientoScreen(viewModel)
        }
        composable(
            route = Screen.VehiculoForm.route,
            arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("vehiculoId") ?: -1L
            com.drivesmart.cx.ui.mobile.screens.VehiculoFormScreen(
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
            com.drivesmart.cx.ui.mobile.screens.GastosScreen(viewModel)
        }
        composable(Screen.Configuracion.route) {
            com.drivesmart.cx.ui.mobile.screens.ConfiguracionScreen(viewModel)
        }
    }
}
