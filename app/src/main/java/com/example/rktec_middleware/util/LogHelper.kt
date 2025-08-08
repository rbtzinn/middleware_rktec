package com.example.rktec_middleware.util

import android.content.Context
import android.os.Environment
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {

    // FUNÇÃO ATUALIZADA: AGORA RECEBE O MAPEAMENTO COMO PARÂMETRO
    fun exportarPlanilhaCompleta(
        context: Context,
        banco: AppDatabase,
        companyId: String,
        mapeamento: MapeamentoPlanilha? // <-- PARÂMETRO NOVO
    ): Flow<ExportProgress> = flow {
        emit(ExportProgress.InProgress(0))

        val prefs = context.getSharedPreferences("inventario_prefs", Context.MODE_PRIVATE)
        val jsonCabecalho = prefs.getString("cabecalho_original", null)
        if (jsonCabecalho == null) {
            emit(ExportProgress.Error("Cabeçalho original não encontrado."))
            return@flow
        }

        val cabecalhoOriginal = mutableListOf<String>().apply {
            val jsonArray = JSONArray(jsonCabecalho)
            for (i in 0 until jsonArray.length()) add(jsonArray.getString(i))
        }

        // AGORA USAMOS O MAPEAMENTO RECEBIDO
        if (mapeamento == null) {
            emit(ExportProgress.Error("Mapeamento da planilha não encontrado."))
            return@flow
        }

        val todosOsItens = banco.inventarioDao().listarTodosPorEmpresa(companyId)
        val totalDeItens = todosOsItens.size
        if (totalDeItens == 0) {
            emit(ExportProgress.Error("Nenhum item no inventário para exportar."))
            return@flow
        }

        val workbook = XSSFWorkbook()
        val sheetInventario = workbook.createSheet("Inventário Final")

        val headerRowInventario = sheetInventario.createRow(0)
        cabecalhoOriginal.forEachIndexed { index, nomeColuna ->
            headerRowInventario.createCell(index).setCellValue(nomeColuna)
        }

        todosOsItens.forEachIndexed { index, item ->
            val row = sheetInventario.createRow(index + 1)
            cabecalhoOriginal.forEachIndexed { colIndex, nomeColuna ->
                // LÓGICA ATUALIZADA PARA USAR OS CAMPOS CORRETOS
                val valor = when (cabecalhoOriginal.indexOf(nomeColuna)) {
                    mapeamento.colunaEpc -> item.tag
                    mapeamento.colunaNome -> item.desc
                    mapeamento.colunaSetor -> item.localizacao
                    mapeamento.colunaLoja -> item.loja
                    else -> item.colunasExtras[nomeColuna] ?: ""
                }
                row.createCell(colIndex).setCellValue(valor)
            }
            val progresso = (((index + 1) / totalDeItens.toFloat()) * 100).toInt()
            emit(ExportProgress.InProgress(progresso))
        }

        val timestamp = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(Date())
        val nomeArquivo = "planilha_final_inventario_${timestamp}.xlsx"

        val pasta = criarPastaRelatorios(context)
        val file = File(pasta, nomeArquivo)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        emit(ExportProgress.Success(file))

    }.catch { e ->
        e.printStackTrace()
        emit(ExportProgress.Error(e.message ?: "Ocorreu um erro desconhecido durante a exportação."))
    }

    suspend fun exportarLogDeEdicoes(context: Context, banco: AppDatabase): File? {
        try {
            val logsDeEdicao = banco.logEdicaoDao().listarTodos()
            if (logsDeEdicao.isEmpty()) return null

            val workbook = XSSFWorkbook()
            val sheetLog = workbook.createSheet("Log de Alterações Manuais")

            val headerRowLog = sheetLog.createRow(0)
            headerRowLog.createCell(0).setCellValue("Data/Hora")
            headerRowLog.createCell(1).setCellValue("Responsável")
            headerRowLog.createCell(2).setCellValue("Tag do Item")
            headerRowLog.createCell(3).setCellValue("Campo Alterado")
            headerRowLog.createCell(4).setCellValue("Valor Antigo")
            headerRowLog.createCell(5).setCellValue("Valor Novo")

            logsDeEdicao.forEachIndexed { index, log ->
                val row = sheetLog.createRow(index + 1)
                row.createCell(0).setCellValue(log.dataHora)
                row.createCell(1).setCellValue(log.usuarioResponsavel)
                row.createCell(2).setCellValue(log.tagDoItem)
                row.createCell(3).setCellValue(log.campoAlterado)
                row.createCell(4).setCellValue(log.valorAntigo)
                row.createCell(5).setCellValue(log.valorNovo)
            }

            val timestamp = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(Date())
            val nomeArquivo = "relatorio_edicoes_manuais_${timestamp}.xlsx"

            val pasta = criarPastaRelatorios(context)
            val file = File(pasta, nomeArquivo)
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace(); return null
        }
    }

    // --- FUNÇÃO 3: REGISTRAR SESSÃO DE INVENTÁRIO ---
    suspend fun registrarSessaoDeInventario(
        context: Context,
        usuario: String,
        loja: String?,
        setor: String?,
        itensEsperados: List<ItemInventario>,
        itensLidos: List<EpcTag>,
        itensTotaisDaBase: List<ItemInventario>
    ): File? {
        try {
            val nomeLoja = loja?.replace(" ", "_")?.uppercase() ?: "GERAL"
            val nomeArquivo = "LOG_INVENTARIO_${nomeLoja}.csv"
            val pasta = criarPastaRelatorios(context)
            val file = File(pasta, nomeArquivo)
            val delimitador = ";"

            file.appendText("\n--- INÍCIO DA SESSÃO DE INVENTÁRIO ---\n", Charsets.UTF_8)
            file.appendText("Responsável;$usuario\n", Charsets.UTF_8)
            file.appendText("Data/Hora;${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n", Charsets.UTF_8)
            file.appendText("Loja;${loja ?: "Todas"}\n", Charsets.UTF_8)
            file.appendText("Setor;${setor ?: "Todos"}\n\n", Charsets.UTF_8)
            file.appendText("Status;Tag EPC;Detalhes\n", Charsets.UTF_8)

            val epcsLidos = itensLidos.map { it.epc }.toSet()
            val epcsEsperadosNoSetor = itensEsperados.map { it.tag }.toSet()

            val encontrados = epcsLidos.intersect(epcsEsperadosNoSetor)
            val naoEncontrados = epcsEsperadosNoSetor - epcsLidos
            val aMais = epcsLidos - epcsEsperadosNoSetor

            encontrados.forEach { file.appendText("ENCONTRADO;$it;Item estava no local esperado.\n", Charsets.UTF_8) }
            naoEncontrados.forEach { file.appendText("NÃO ENCONTRADO;$it;Item esperado neste setor, mas não foi lido.\n", Charsets.UTF_8) }

            aMais.forEach { epc ->
                val itemNaBase = itensTotaisDaBase.find { it.tag == epc }
                when {
                    itemNaBase == null -> file.appendText("INEXISTENTE NA BASE;$epc;Item lido, mas não consta na planilha original.\n", Charsets.UTF_8)
                    itemNaBase.loja != loja -> file.appendText("EM OUTRA LOJA;$epc;Item lido, mas pertence à loja '${itemNaBase.loja}'.\n", Charsets.UTF_8)
                    else -> file.appendText("MOVIDO DE SETOR;$epc;Item pertence a esta loja, mas estava no setor '${itemNaBase.localizacao}'. Foi corrigido.\n", Charsets.UTF_8)
                }
            }

            file.appendText("--- FIM DA SESSÃO ---\n", Charsets.UTF_8)
            return file
        } catch (e: Exception) {
            e.printStackTrace(); return null
        }
    }

    // --- FUNÇÕES DE LOG DE USUÁRIOS (RESTAURADAS) ---
    suspend fun registrarGerenciamentoUsuario(
        context: Context,
        companyId: String,
        usuarioResponsavel: String,
        acao: String,
        usuarioAlvo: String,
        motivo: String?,
        detalhes: String
    ) {
        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val db = AppDatabase.getInstance(context.applicationContext)
        db.logGerenciamentoUsuarioDao().inserir(
            LogGerenciamentoUsuario(
                id = 0,
                companyId = companyId,
                usuarioResponsavel = usuarioResponsavel,
                dataHora = dataHora,
                acao = acao,
                usuarioAlvo = usuarioAlvo,
                motivo = motivo,
                detalhes = detalhes
            )
        )
    }

    // ✅ FIX: Removido o parâmetro companyId, pois a busca no DAO não o utiliza.
    suspend fun exportarLogsGerenciamentoUsuarioXlsx(context: Context): File {
        val db = AppDatabase.getInstance(context)
        // ✅ FIX: Chamada ao banco de dados corrigida para não passar o companyId.
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

        val nomeArquivo = "relatorio_log_usuarios.xlsx"
        val pasta = criarPastaRelatorios(context)
        val file = File(pasta, nomeArquivo)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // --- FUNÇÕES AUXILIARES ---
    private fun formatarCelulaCsv(valor: String): String {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"${valor.replace("\"", "\"\"")}\""
        }
        return valor
    }

    private fun criarPastaRelatorios(context: Context): File {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pastaRelatorios = File(downloads, "RelatoriosInventario")
        if (!pastaRelatorios.exists()) pastaRelatorios.mkdirs()
        return pastaRelatorios
    }
}
