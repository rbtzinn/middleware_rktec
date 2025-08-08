package com.example.rktec_middleware.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rktec_middleware.data.dao.*
import com.example.rktec_middleware.data.model.*

@Database(
    entities = [
        ItemInventario::class, EpcTag::class, LogMapeamento::class,
        Usuario::class, LogGerenciamentoUsuario::class, SessaoInventario::class,
        ItemSessao::class, LogEdicaoItem::class
    ],
    version = 24,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun logMapeamentoDao(): LogMapeamentoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun logGerenciamentoUsuarioDao(): LogGerenciamentoUsuarioDao
    abstract fun logEdicaoDao(): LogEdicaoDao
    abstract fun historicoDao(): HistoricoDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "rktec.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}