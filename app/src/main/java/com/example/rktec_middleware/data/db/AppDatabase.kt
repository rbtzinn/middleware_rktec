package com.example.rktec_middleware.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rktec_middleware.data.dao.ColetaDao
import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.data.dao.MapeamentoDao
import com.example.rktec_middleware.data.model.MapeamentoPlanilha


@Database(entities = [ItemInventario::class, EpcTag::class, MapeamentoPlanilha::class], version = 7)

abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun mapeamentoDao(): MapeamentoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rktec.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

            }
        }
    }
}
