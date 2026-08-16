package com.drivesmart.cx.di

import com.drivesmart.cx.data.repository.DriveSmartRepositoryImpl
import com.drivesmart.cx.data.repository.VehicleRepositoryImpl
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindAssistantRepository(impl: DriveSmartRepositoryImpl): DriveSmartRepository
}
