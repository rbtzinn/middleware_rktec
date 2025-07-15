package com.example.rktec_middleware.data.db

import androidx.room.TypeConverter
import org.json.JSONObject

// NOVO: Classe inteira para permitir que o Room salve tipos de dados complexos.
class Converters {
    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String? {
        return map?.let { JSONObject(it).toString() }
    }

    @TypeConverter
    fun toStringMap(jsonString: String?): Map<String, String>? {
        return jsonString?.let {
            val jsonObject = JSONObject(it)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            map
        }
    }
}