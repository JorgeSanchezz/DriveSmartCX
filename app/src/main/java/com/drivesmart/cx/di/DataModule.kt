package com.drivesmart.cx.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.drivesmart.cx.data.local.dao.*
import com.drivesmart.cx.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bitacora_puntos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `viajeId` INTEGER NOT NULL,
                    `latitud` REAL NOT NULL,
                    `longitud` REAL NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    FOREIGN KEY(`viajeId`) REFERENCES `bitacora`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bitacora_puntos_viajeId` ON `bitacora_puntos` (`viajeId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "assistant_db"
        )
        .addMigrations(MIGRATION_11_12)
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()
    }

    @Provides
    fun provideVehiculoDao(db: AppDatabase): VehiculoDao = db.vehiculoDao()

    @Provides
    fun provideGastoDao(db: AppDatabase): GastoDao = db.gastoDao()

    @Provides
    fun provideServicioDao(db: AppDatabase): ServicioDao = db.servicioDao()

    @Provides
    fun provideTramiteDao(db: AppDatabase): TramiteDao = db.tramiteDao()

    @Provides
    fun provideBitacoraDao(db: AppDatabase): BitacoraDao = db.bitacoraDao()

    @Provides
    fun provideBitacoraPuntoDao(db: AppDatabase): BitacoraPuntoDao = db.bitacoraPuntoDao()

    @Provides
    fun provideContactoDao(db: AppDatabase): ContactoDao = db.contactoDao()

    @Provides
    fun provideUbicacionDao(db: AppDatabase): UbicacionDao = db.ubicacionDao()

    @Provides
    fun provideSeguroDao(db: AppDatabase): SeguroDao = db.seguroDao()

    @Provides
    fun providePreventivoDao(db: AppDatabase): PreventivoDao = db.preventivoDao()

    @Provides
    fun provideContactoEmergenciaDao(db: AppDatabase): ContactoEmergenciaDao = db.contactoEmergenciaDao()

    @Provides
    fun provideErrorLogDao(db: AppDatabase): ErrorLogDao = db.errorLogDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("drivesmart_prefs", Context.MODE_PRIVATE)
    }
}
