package com.example.rktec_middleware.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rktec_middleware.data.dao.LogMapeamentoDao
import com.example.rktec_middleware.data.model.LogMapeamento
import com.example.rktec_middleware.data.dao.ColetaDao
import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.dao.LogEdicaoDao
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario
import com.example.rktec_middleware.data.dao.LogGerenciamentoUsuarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.data.dao.MapeamentoDao
// NOVO: Import da nova classe de entidade
import com.example.rktec_middleware.data.model.LogEdicaoItem
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.data.dao.UsuarioDao

@Database(
    entities = [
        ItemInventario::class,
        EpcTag::class,
        MapeamentoPlanilha::class,
        LogMapeamento::class,
        Usuario::class,
        LogGerenciamentoUsuario::class,
        // MODIFICADO: Adicionada a nova entidade à lista do banco de dados.
        LogEdicaoItem::class
    ],
    // MODIFICADO: Versão incrementada de 12 para 13 devido à mudança na estrutura.
    version = 14
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun mapeamentoDao(): MapeamentoDao
    abstract fun logMapeamentoDao(): LogMapeamentoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun logGerenciamentoUsuarioDao(): LogGerenciamentoUsuarioDao
    abstract fun logEdicaoDao(): LogEdicaoDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rktec.db"
                )
                    // fallbackToDestructiveMigration irá apagar e recriar o banco
                    // ao encontrar uma nova versão. Ideal para desenvolvimento.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}