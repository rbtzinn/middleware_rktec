package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario")
data class ItemInventario(
    @PrimaryKey val tag: String,
    val desc: String
)
