package com.drivesmart.cx.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.drivesmart.cx.data.local.dao.*
import com.drivesmart.cx.data.local.entity.*

@Database(
    entities = [
        VehiculoEntity::class,
        GastoEntity::class,
        ServicioEntity::class,
        TramiteEntity::class,
        BitacoraEntity::class,
        BitacoraPuntoEntity::class,
        ContactoEntity::class,
        UbicacionEntity::class,
        SeguroEntity::class,
        PreventivoEntity::class,
        ContactoEmergenciaEntity::class,
        ErrorLogEntity::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun gastoDao(): GastoDao
    abstract fun servicioDao(): ServicioDao
    abstract fun tramiteDao(): TramiteDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun bitacoraPuntoDao(): BitacoraPuntoDao
    abstract fun contactoDao(): ContactoDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun seguroDao(): SeguroDao
    abstract fun preventivoDao(): PreventivoDao
    abstract fun contactoEmergenciaDao(): ContactoEmergenciaDao
    abstract fun errorLogDao(): ErrorLogDao
}
