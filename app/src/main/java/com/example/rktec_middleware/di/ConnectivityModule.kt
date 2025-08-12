// Em um novo arquivo, ex: di/ConnectivityModule.kt
package com.example.rktec_middleware.di // Ajuste o package se necessário

import com.example.rktec_middleware.util.ConnectivityObserver
import com.example.rktec_middleware.util.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        networkConnectivityObserver: NetworkConnectivityObserver
    ): ConnectivityObserver
}