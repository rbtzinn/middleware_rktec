package com.example.rktec_middleware.util

import android.content.Context
import android.net.Uri
import com.example.rktec_middleware.data.model.ItemInventario
import java.io.BufferedReader
import java.io.InputStreamReader

object LeitorInventario {
    fun lerCsv(context: Context, uri: Uri): List<ItemInventario> {
        val resultado = mutableListOf<ItemInventario>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val linhas = reader.readLines()
        if (linhas.isEmpty()) return emptyList()

        val cabecalho = linhas[0].split(',').map { it.trim().lowercase().replace("\"", "") }
        val indexTag = cabecalho.indexOfFirst { it == "tag" || it == "epc" }
        val indexDesc = cabecalho.indexOfFirst { it == "desc.item" || it == "descricao" || it == "nome" || it == "item" }

        if (indexTag == -1 || indexDesc == -1) return emptyList()

        for (linha in linhas.drop(1)) {
            val colunas = linha.split(',').map { it.trim() }
            if (colunas.size <= maxOf(indexTag, indexDesc)) continue
            val tag = colunas[indexTag]
            val desc = colunas[indexDesc]
            if (tag.isNotBlank()) resultado.add(ItemInventario(tag, desc))
        }
        return resultado
    }

}
