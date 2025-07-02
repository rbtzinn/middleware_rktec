package com.example.rktec_middleware.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rktec_middleware.data.model.ItemInventario
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer

object LeitorInventario {
    fun normalizaCampo(campo: String): String {
        // Tira acento, espaço, pontuação e coloca minúsculo
        return Normalizer.normalize(campo, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
            .replace("[^a-zA-Z0-9]".toRegex(), "")
            .lowercase()
    }

    val apelidosTag = setOf("tag", "epc", "etiqueta", "codigo", "codigorfid", "codigodebarra")
    val apelidosDesc = setOf("descricao", "desc", "detalhe", "item", "descdetalhada", "nome", "produto", "descitem")
    val apelidosSetor = setOf("localizacao", "setor", "codsetor", "local", "posicao")


    fun lerCsv(context: Context, uri: Uri): List<ItemInventario> {
        val resultado = mutableListOf<ItemInventario>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        val linhas = reader.readLines().filter { it.isNotBlank() }
        if (linhas.isEmpty()) return emptyList()

        // Detecta delimitador
        val delimitadores = listOf(";", ",", "\t", "|")
        val delimitador = delimitadores.maxByOrNull { linhas[0].count { ch -> ch == it[0] } } ?: ","

        // Trata cabeçalho universal
        val cabecalhoBruto = linhas[0]
            .replace("\"", "")
            .replace("\uFEFF", "")
            .split(delimitador)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val cabecalhoNormalizado = cabecalhoBruto.map { normalizaCampo(it) }

        // Pega índice das colunas
        val indexTag = cabecalhoNormalizado.indexOfFirst { apelidosTag.contains(it) }
        val indexDesc = cabecalhoNormalizado.indexOfFirst { apelidosDesc.contains(it) }
        val indexSetor = cabecalhoNormalizado.indexOfFirst { apelidosSetor.contains(it) }


        if (indexTag == -1) throw Exception("Coluna de TAG/RFID não encontrada!")
        // Desc é opcional, mas se não existir, põe string vazia
        // Não lança erro se desc não existir

        for (linha in linhas.drop(1)) {
            if (linha.isBlank()) continue
            val colunas = linha.replace("\"", "").split(delimitador).map { it.trim() }
            if (colunas.size <= indexTag) continue

            val tag = colunas[indexTag]
            val desc = if (indexDesc != -1 && colunas.size > indexDesc) colunas[indexDesc] else ""
            val setor = if (indexSetor != -1 && colunas.size > indexSetor) colunas[indexSetor] else ""
            if (tag.isNotBlank()) {
                resultado.add(ItemInventario(tag, desc, setor))
            }
        }
        return resultado
    }



    fun lerExcel(context: Context, uri: Uri): List<ItemInventario> {
        val lista = mutableListOf<ItemInventario>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            val headerRow = sheet.getRow(0) ?: return emptyList()

            var indexTag = -1
            var indexDesc = -1
            var indexSetor = -1

            for (cell in headerRow) {
                val valor = cell.toString().trim().lowercase().replace(" ", "")
                if (valor == "tag" || valor == "epc") indexTag = cell.columnIndex
                if (valor == "desc.item" || valor == "descricao" || valor == "nome" || valor == "item") indexDesc = cell.columnIndex
                if (apelidosSetor.contains(valor)) indexSetor = cell.columnIndex
            }

            Log.d("LeitorExcel", "IndexTag: $indexTag, IndexDesc: $indexDesc")

            if (indexTag == -1 || indexDesc == -1) {
                Log.d("LeitorExcel", "Colunas obrigatórias não encontradas!")
                return emptyList()
            }

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val tag = row.getCell(indexTag)?.toString()?.trim()
                val desc = row.getCell(indexDesc)?.toString()?.trim() ?: ""
                val setor = if (indexSetor != -1) row.getCell(indexSetor)?.toString()?.trim() ?: "" else ""
                if (!tag.isNullOrBlank()) {
                    lista.add(ItemInventario(tag, desc, setor))
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

        Log.d("LeitorExcel", "Total de itens importados: ${lista.size}")
        return lista
    }
}
