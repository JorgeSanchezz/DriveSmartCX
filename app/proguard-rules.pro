# Reglas para mantener los modelos de datos de DriveSmartCX
# Esto es CRÍTICO para que el respaldo (JSON) funcione en la versión de Google Play

# Mantener la clase BackupData y todos sus campos
-keep class com.drivesmart.cx.domain.repository.BackupData { *; }
-keepclassmembers class com.drivesmart.cx.domain.repository.BackupData { <fields>; }

# Mantener todas las entidades de la base de datos
-keep class com.drivesmart.cx.data.local.entity.** { *; }
-keepclassmembers class com.drivesmart.cx.data.local.entity.** { <fields>; }

# Reglas generales para la serialización con Gson
-keepattributes Signature, *Annotation*, EnclosingMethod
-keepclassmembers enum * { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Prevenir que R8 elimine constructores vacíos si Gson los necesita
-keepclassmembers class com.drivesmart.cx.domain.repository.BackupData {
    <init>(...);
}
-keepclassmembers class com.drivesmart.cx.data.local.entity.** {
    <init>(...);
}
