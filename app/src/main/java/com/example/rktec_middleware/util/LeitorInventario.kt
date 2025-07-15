package com.example.rktec_middleware.util

import android.content.Context
import android.net.Uri
import android.util.Log
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

object LeitorInventario {

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cell.dateCellValue)
                } else {
                    val num = cell.numericCellValue
                    if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try { cell.stringCellValue } catch (e: Exception) { "" }
            else -> ""
        }
    }

    fun lerDadosBrutosDaPlanilha(context: Context, uri: Uri): Pair<List<String>, List<List<String>>>? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType = getMimeType(context, uri)

            return when {
                // Rota para CSV
                mimeType?.contains("csv") == true || mimeType?.contains("comma-separated-values") == true -> lerCsv(inputStream)
                // Rota para Excel
                mimeType?.contains("spreadsheet") == true || mimeType?.contains("excel") == true -> lerExcel(inputStream)
                else -> {
                    // Fallback para arquivos sem MimeType (tentativa pela extensão)
                    val filename = uri.lastPathSegment?.lowercase()
                    if (filename?.endsWith(".csv") == true) lerCsv(inputStream)
                    else if (filename?.endsWith(".xls") == true || filename?.endsWith(".xlsx") == true) lerExcel(inputStream)
                    else null
                }
            }.also {
                inputStream.close()
            }
        } catch (e: Exception) {
            Log.e("LeitorInventario", "Erro ao ler dados brutos", e)
            return null
        }
    }

    // MODIFICADO: Esta função agora lê o arquivo linha por linha, sendo mais eficiente para arquivos grandes.
    private fun lerCsv(inputStream: InputStream): Pair<List<String>, List<List<String>>> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        // Lê apenas a primeira linha para o cabeçalho
        val cabecalhoLine = reader.readLine()
        if (cabecalhoLine.isNullOrEmpty()) {
            reader.close()
            return Pair(emptyList(), emptyList())
        }

        // Detecta o delimitador com base no cabeçalho
        val delimitador = listOf(";", ",", "\t").maxByOrNull { cabecalhoLine.count { c -> c == it[0] } } ?: ","
        val cabecalho = cabecalhoLine.split(delimitador).map { it.trim().removeSurrounding("\"") }

        val dados = mutableListOf<List<String>>()
        // Lê o restante do arquivo linha por linha
        reader.forEachLine { linha ->
            if (linha.isNotBlank()) {
                val valores = linha.split(delimitador).map { it.trim().removeSurrounding("\"") }
                dados.add(valores)
            }
        }

        reader.close()
        return Pair(cabecalho, dados)
    }

    private fun lerExcel(inputStream: InputStream): Pair<List<String>, List<List<String>>> {
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        val cabecalho = sheet.getRow(0)?.map { getCellValueAsString(it) } ?: emptyList()
        val dados = (1..sheet.lastRowNum).map { i ->
            val row = sheet.getRow(i)
            cabecalho.indices.map { j ->
                getCellValueAsString(row?.getCell(j, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))
            }
        }
        workbook.close()
        return Pair(cabecalho, dados)
    }
}