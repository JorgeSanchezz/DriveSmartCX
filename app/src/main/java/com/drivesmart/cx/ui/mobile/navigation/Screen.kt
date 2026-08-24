package com.drivesmart.cx.ui.mobile.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object VehiculoForm : Screen("vehiculo_form/{vehiculoId}") {
        fun createRoute(id: Long = -1L) = "vehiculo_form/$id"
    }
    object Gastos : Screen("gastos")
    object Servicios : Screen("servicios")
    object Tramites : Screen("tramites")
    object Emergencias : Screen("emergencias")
    object InformacionVehiculo : Screen("info_vehiculo")
    object Garage : Screen("garage")
    object Bitacora : Screen("bitacora")
    object Estacionamiento : Screen("estacionamiento")
    object Configuracion : Screen("configuracion")
    object Seguro : Screen("seguro")
    object Preventivos : Screen("preventivos")
    object SOSConfig : Screen("sos_config")
    object ErrorLogs : Screen("error_logs")
}
