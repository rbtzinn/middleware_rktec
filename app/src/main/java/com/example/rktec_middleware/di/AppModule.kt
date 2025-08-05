package com.example.rktec_middleware.di

import android.app.Application
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson // IMPORT ADICIONADO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Adicionado de volta: ensina o Hilt a criar um objeto Gson.
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

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

    // Atualizado para receber o Gson
    @Provides
    @Singleton
    fun provideInventarioRepository(db: AppDatabase, gson: Gson): InventarioRepository {
        return InventarioRepository(db.inventarioDao(), gson)
    }

    @Provides
    @Singleton
    fun provideHistoricoRepository(db: AppDatabase): HistoricoRepository {
        return HistoricoRepository(db.historicoDao())
    }
}