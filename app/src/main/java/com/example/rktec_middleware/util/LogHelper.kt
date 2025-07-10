package com.example.rktec_middleware.util

import android.content.Context
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.LogMapeamento
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {


    suspend fun registrarGerenciamentoUsuario(
        context: Context,
        usuarioResponsavel: String,
        acao: String, // "EDIÇÃO" ou "EXCLUSÃO"
        usuarioAlvo: String,
        motivo: String?,
        detalhes: String
    ) {
        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val db = AppDatabase.getInstance(context.applicationContext)
        db.logGerenciamentoUsuarioDao().inserir(
            LogGerenciamentoUsuario(
                usuarioResponsavel = usuarioResponsavel,
                dataHora = dataHora,
                acao = acao,
                usuarioAlvo = usuarioAlvo,
                motivo = motivo,
                detalhes = detalhes
            )
        )
    }



    suspend fun registrarMapeamento(context: Context, usuario: String, arquivo: String) {
        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val db = AppDatabase.getInstance(context)
        db.logMapeamentoDao().inserir(
            LogMapeamento(
                usuario = usuario,
                dataHora = dataHora,
                arquivo = arquivo
            )
        )
    }

    suspend fun exportarLogsGerenciamentoUsuarioXlsx(
        context: Context,
        nomeArquivo: String = "relatorio_log_usuarios.xlsx"
    ): File {
        val db = AppDatabase.getInstance(context)
        val logs = db.logGerenciamentoUsuarioDao().listarTodos()
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Log Usuários")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Responsável")
        header.createCell(1).setCellValue("DataHora")
        header.createCell(2).setCellValue("Ação")
        header.createCell(3).setCellValue("Usuário Alvo")
        header.createCell(4).setCellValue("Motivo")
        header.createCell(5).setCellValue("Detalhes")

        logs.forEachIndexed { index, log ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(log.usuarioResponsavel)
            row.createCell(1).setCellValue(log.dataHora)
            row.createCell(2).setCellValue(log.acao)
            row.createCell(3).setCellValue(log.usuarioAlvo)
            row.createCell(4).setCellValue(log.motivo ?: "")
            row.createCell(5).setCellValue(log.detalhes)
        }

        val pasta = criarPastaRelatorios(context)
        val file = File(pasta, nomeArquivo)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }


    suspend fun exportarLogsComoCsv(context: Context, nomeArquivo: String = "relatorio_log.csv"): File {
        val db = AppDatabase.getInstance(context)
        val logs = db.logMapeamentoDao().listarTodos()
        val csv = StringBuilder()
        csv.append("Usuário,DataHora,Arquivo\n")
        logs.forEach { log ->
            csv.append("\"${log.usuario}\",\"${log.dataHora}\",\"${log.arquivo}\"\n")
        }

        val pasta = criarPastaRelatorios(context)
        val arquivo = File(pasta, nomeArquivo)
        FileOutputStream(arquivo).use { fos ->
            fos.write(csv.toString().toByteArray())
        }
        return arquivo
    }

    private fun criarPastaRelatorios(context: Context): File {
        val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val pastaRelatorios = File(downloads, "relatorios")
        if (!pastaRelatorios.exists()) pastaRelatorios.mkdirs()
        return pastaRelatorios
    }



    suspend fun exportarRelatorioMapeamentoXlsx(
        context: Context,
        usuario: String,
        arquivo: String,
        mapeamento: com.example.rktec_middleware.data.model.MapeamentoPlanilha,
        nomesColunas: List<String>
    ): File? {
        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Relatório de Mapeamento")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Usuário")
        header.createCell(1).setCellValue("Data/Hora")
        header.createCell(2).setCellValue("Arquivo Importado")
        header.createCell(3).setCellValue("EPC")
        header.createCell(4).setCellValue("Nome")
        header.createCell(5).setCellValue("Setor")
        header.createCell(6).setCellValue("Loja")

        // Linha de dados
        val row = sheet.createRow(1)
        row.createCell(0).setCellValue(usuario)
        row.createCell(1).setCellValue(dataHora)
        row.createCell(2).setCellValue(arquivo)

        fun nomeCol(idx: Int?): String =
            if (idx != null && idx in nomesColunas.indices) nomesColunas[idx] else "Não mapeada"

        row.createCell(3).setCellValue(nomeCol(mapeamento.colunaEpc))
        row.createCell(4).setCellValue(nomeCol(mapeamento.colunaNome))
        row.createCell(5).setCellValue(nomeCol(mapeamento.colunaSetor))
        row.createCell(6).setCellValue(nomeCol(mapeamento.colunaLoja))

        val pasta = criarPastaRelatorios(context)
        val file = File(pasta, "relatorio_de_mapeamento.xlsx")
        try {
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            workbook.close()
            return null
        }
    }

}

