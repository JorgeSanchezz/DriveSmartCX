# Guía Post-Desarrollo: Dodge Attitude Assistant

Felicidades, Arquitecto. La aplicación **Dodge Attitude Assistant** está técnicamente completa. Ahora debemos proceder con la validación en entorno real y la preparación para distribución.

## 1. Configuración de Pruebas en Android Auto

Para que el sistema reconozca tu aplicación durante el desarrollo (ya que no proviene de la Play Store), debes habilitar las fuentes desconocidas en la app de Android Auto de tu dispositivo físico.

### Pasos en el Teléfono:
1.  Ve a **Ajustes** > **Google** > **Ajustes de aplicaciones de Google** > **Android Auto**.
2.  Desliza hasta el final donde dice **Versión** y toca 10 veces consecutivas para activar el "Modo Desarrollador".
3.  Toca el menú de tres puntos (arriba a la derecha) y selecciona **Ajustes de desarrollador**.
4.  Marca la casilla **Fuentes desconocidas**.
5.  Asegúrate de que la opción **Recopilar registros de volcado** esté habilitada para debugging.

---

## 2. Pruebas con la Desktop Head Unit (DHU)

Si no tienes un vehículo físico a mano, utilizaremos el emulador oficial de Google.

### Preparación del Entorno:
1.  En Android Studio, abre el **SDK Manager**.
2.  En **SDK Tools**, busca e instala **Android Auto Desktop Head Unit emulator**.
3.  Conecta tu teléfono físico por USB.
4.  Ejecuta el siguiente comando en la terminal para redirigir el puerto:
    ```bash
    adb forward tcp:5277 tcp:5277
    ```
5.  En el teléfono, dentro de los Ajustes de desarrollador de Android Auto, selecciona **Iniciar servidor de la unidad de cabecera**.
6.  Ejecuta el ejecutable `desktop-head-unit.exe` ubicado en tu SDK:
    `AppData\Local\Android\Sdk\extras\google\auto\desktop-head-unit.exe`

---

## 3. Compilación del APK / App Bundle

Para la entrega final o pruebas internas (Internal App Sharing):

### Generar APK de Debug:
Puedes generar un APK rápido para pruebas directas:
```bash
./gradlew assembleDebug
```

### Generar Release firmado:
1.  Ve a **Build** > **Generate Signed Bundle / APK...**
2.  Selecciona **Android App Bundle** (recomendado para Play Store).
3.  Crea o selecciona tu `Keystore`.
4.  Asegúrate de seleccionar la variante `release`.
5.  El archivo resultante estará en `app/release/app-release.aab`.

---

## 4. Checklist de Verificación Final

> [!IMPORTANT]
> **Permisos de Ubicación**: Para la Bitácora de Viaje, el usuario debe otorgar el permiso "Permitir siempre" para que funcione con la pantalla del teléfono apagada.
>
> **Biometría**: Si el dispositivo no tiene sensor de huellas configurado, la app caerá al método de respaldo (PIN/Patrón).

> [!TIP]
> **Iconografía**: Asegúrate de que los iconos utilizados en las plantillas de `CarAppLibrary` sean `IconCompat` y tengan suficiente contraste (blanco sobre fondo oscuro por defecto en el auto).

---

## Resumen del Proyecto
- **Arquitectura**: Clean Architecture + MVVM + Hilt.
- **Persistencia**: Room Database con relaciones 1:N y integridad referencial.
- **Seguridad**: Autenticación Biométrica Mandataria.
- **Automotriz**: Integración completa con plantillas seguras de Android Auto.
- **Exportación**: Backups JSON y Reportes CSV listos para producción.
