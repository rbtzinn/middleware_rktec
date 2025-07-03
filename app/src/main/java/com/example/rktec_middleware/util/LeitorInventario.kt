package com.example.rktec_middleware.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer

object LeitorInventario {
    fun normalizaCampo(campo: String): String {
        return Normalizer.normalize(campo, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
            .replace("[^a-zA-Z0-9]".toRegex(), "")
            .lowercase()
    }

    fun lerCsv(context: Context, uri: Uri, mapeamento: MapeamentoPlanilha): List<ItemInventario> {
        val resultado = mutableListOf<ItemInventario>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        val linhas = reader.readLines().filter { it.isNotBlank() }
        if (linhas.isEmpty()) return emptyList()

        val delimitadores = listOf(";", ",", "\t", "|")
        val delimitador = delimitadores.maxByOrNull { linhas[0].count { ch -> ch == it[0] } } ?: ","

        val cabecalhoBruto = linhas[0]
            .replace("\"", "")
            .replace("\uFEFF", "")
            .split(delimitador)
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (linha in linhas.drop(1)) {
            if (linha.isBlank()) continue
            val colunas = linha.replace("\"", "").split(delimitador).map { it.trim() }
            if (colunas.size <= mapeamento.colunaEpc) continue

            val tag = colunas.getOrNull(mapeamento.colunaEpc) ?: ""
            val desc = mapeamento.colunaNome?.let { colunas.getOrNull(it) } ?: ""
            val setor = mapeamento.colunaSetor?.let { colunas.getOrNull(it) } ?: ""
            val loja = mapeamento.colunaLoja?.let { colunas.getOrNull(it) } ?: ""
            if (tag.isNotBlank()) {
                resultado.add(ItemInventario(tag, desc, setor, loja))
            }
        }
        return resultado
    }

    fun lerExcel(context: Context, uri: Uri, mapeamento: MapeamentoPlanilha): List<ItemInventario> {
        val lista = mutableListOf<ItemInventario>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val tag = row.getCell(mapeamento.colunaEpc)?.toString()?.trim() ?: ""
                val desc = mapeamento.colunaNome?.let { row.getCell(it)?.toString()?.trim() ?: "" } ?: ""
                val setor = mapeamento.colunaSetor?.let { row.getCell(it)?.toString()?.trim() ?: "" } ?: ""
                val loja = mapeamento.colunaLoja?.let { row.getCell(it)?.toString()?.trim() ?: "" } ?: ""
                if (tag.isNotBlank()) {
                    lista.add(ItemInventario(tag, desc, setor, loja))
                }

            }
            workbook.close()
        } catch (e: OutOfMemoryError) {
            Log.e("LeitorExcel", "Arquivo Excel muito grande: ${e.message}")
            return emptyList()
        } catch (e: Exception) {
            Log.e("LeitorExcel", "Erro ao ler Excel: ${e.message}")
            return emptyList()
        }
        return lista
    }

}
