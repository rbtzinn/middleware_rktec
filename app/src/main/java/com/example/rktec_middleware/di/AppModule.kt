package com.example.rktec_middleware.di

import android.app.Application
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(db: AppDatabase): UsuarioRepository {
        return UsuarioRepository(db.usuarioDao())
    }

    @Provides
    @Singleton
    fun provideInventarioRepository(db: AppDatabase): InventarioRepository {
        return InventarioRepository(db.inventarioDao())
    }
}