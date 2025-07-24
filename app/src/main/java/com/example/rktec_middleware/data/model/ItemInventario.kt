package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.rktec_middleware.data.db.Converters

@Entity(tableName = "itens_inventario")
@TypeConverters(Converters::class)
data class ItemInventario(
    @PrimaryKey val tag: String,
    var desc: String = "",
    var localizacao: String = "",
    var loja: String = "",
    val colunasExtras: Map<String, String> = emptyMap(),
    val companyId: String
)