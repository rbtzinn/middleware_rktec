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

@Database(entities = [ItemInventario::class, EpcTag::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun coletaDao(): ColetaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // MIGRAÇÃO de v2 para v3: adiciona coluna localizacao
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE inventario ADD COLUMN localizacao TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rktec.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
