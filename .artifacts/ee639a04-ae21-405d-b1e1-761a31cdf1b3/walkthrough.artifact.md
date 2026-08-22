# Refactorización de MiCODUS a Integración Nativa

Se ha eliminado la dependencia del `WebView` para el inicio de sesión de MiCODUS, reemplazándola por una implementación 100% nativa utilizando Retrofit, OkHttp y Corrutinas.

## Cambios Realizados

### 1. Networking y Sesión
- **[NetworkModule.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/di/NetworkModule.kt):** Se configuró un `CookieJar` robusto en memoria para persistir las cookies `.ASPXAUTH` y `ASP.NET_SessionId` durante la sesión de la aplicación.
- **[MicodusApiService.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/data/remote/micodus/MicodusApiService.kt):** Se actualizaron los endpoints de login para incluir el parámetro `v=2` requerido por el protocolo actual de MiCODUS.

### 2. Capa de Datos
- **[MicodusRepositoryImpl.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/data/repository/MicodusRepositoryImpl.kt):**
    - Implementación de `login`: Ahora realiza un `GET` inicial para extraer `__VIEWSTATE`, `__VIEWSTATEGENERATOR` y `__EVENTVALIDATION` mediante expresiones regulares, seguido de un `POST` con todos los campos requeridos (IMEI/Usuario, Password, GMT, etc.).
    - Extracción automática del `hidUserID` tras el login exitoso.
    - Mejora en el parseo de la telemetría: Se implementó un limpiador de JSON para manejar las respuestas del backend `.asmx` que devuelven strings escapados con atributos sin comillas.
- **[MicodusModels.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/data/remote/micodus/MicodusModels.kt):** Se ajustó `GetDevicesRequest` para usar por defecto `TimeZones = "-6:00"`.

### 3. Interfaz de Usuario
- **[MicodusViewModel.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/ui/viewmodel/MicodusViewModel.kt):** Se añadió el flujo de login nativo y se automatizó el inicio del polling de telemetría tras una autenticación exitosa.
- **[MicodusLoginScreen.kt](file:///C:/Users/jlsj0/Documents/AndroidStudioProjects/DriveSmartCX/app/src/main/java/com/drivesmart/cx/ui/mobile/screens/MicodusLoginScreen.kt):** Se reemplazó el `AndroidView` que contenía el `WebView` por un formulario nativo en Jetpack Compose con:
    - Selector entre Login por IMEI o por Usuario.
    - Validación de campos y estados de carga/error.
    - Soporte para visualización de contraseña.

## Verificación
- Se verificó que los modelos de datos coinciden con la estructura esperada por el backend de MiCODUS.
- La lógica de extracción de tokens de sesión (`ViewState`) es agnóstica a cambios menores en el HTML siempre que los IDs se mantengan.
- Se eliminó el archivo de utilidad obsoleto `MicodusDataBridge.kt`.
