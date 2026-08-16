# Sincronización de Vehículo Seleccionado entre Móvil y Auto

Este plan tiene como objetivo sincronizar el vehículo seleccionado para que los cambios realizados en la aplicación móvil se reflejen en Android Auto y viceversa. La fuente de verdad será el campo `isSelected` en la base de datos (Room).

## User Review Required

> [!IMPORTANT]
> El cambio implica que el `ViewModel` de la aplicación móvil ahora dependerá directamente del estado en la base de datos para determinar qué vehículo está seleccionado, eliminando el estado volátil en memoria que tenía anteriormente.

## Proposed Changes

### [Mobile/Common] ViewModel

#### [MODIFY] [DriveSmartViewModel.kt](file:///D:/Jorge/Descargas/DodgeAttitudeAssistant2/app/src/main/java/com/drivesmart/cx/ui/viewmodel/DriveSmartViewModel.kt)
- Modificar `selectedVehicleId` para que observe el flujo de vehículos de la base de datos.
- Actualizar `selectVehicle` para que solo modifique la base de datos, permitiendo que el flujo reactivo actualice el estado.
- Eliminar el uso de `_selectedVehicleId` como `MutableStateFlow` manual.

### [Car] Android Auto Screens

#### [MODIFY] [MainScreen.kt](file:///D:/Jorge/Descargas/DodgeAttitudeAssistant2/app/src/main/java/com/drivesmart/cx/car/screens/MainScreen.kt)
- Corregir `startViaje` para que utilice el vehículo seleccionado actualmente (`currentVehicle`) en lugar de simplemente el primero de la lista.
- Asegurar que el nombre y KM mostrados siempre correspondan al vehículo seleccionado en la base de datos.

## Verification Plan

### Manual Verification
1. Abrir la aplicación móvil y seleccionar un vehículo.
2. Abrir el simulador de Android Auto y verificar que el vehículo seleccionado sea el mismo.
3. Cambiar el vehículo en Android Auto (Garage) y verificar que en la aplicación móvil cambien los datos (Gastos, Bitácora, etc.) automáticamente.
4. Iniciar un viaje en Android Auto y verificar que se registre para el vehículo correcto.
