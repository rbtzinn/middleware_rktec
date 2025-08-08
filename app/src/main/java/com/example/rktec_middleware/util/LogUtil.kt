package com.example.rktec_middleware.util

import android.content.Context
import com.example.rktec_middleware.util.LogHelper
import java.io.File

object LogUtil {
    suspend fun logAcaoGerenciamentoUsuario(
        context: Context,
        companyId: String, // ✅ FIX: Adicionado companyId
        usuarioResponsavel: String,
        acao: String,
        usuarioAlvo: String,
        motivo: String? = null,
        detalhes: String = ""
    ): File? { // agora pode retornar null
        LogHelper.registrarGerenciamentoUsuario(
            context = context,
            companyId = companyId, // ✅ FIX: Repassando o companyId
            usuarioResponsavel = usuarioResponsavel,
            acao = acao,
            usuarioAlvo = usuarioAlvo,
            motivo = motivo,
            detalhes = detalhes
        )
        return try {
            // ✅ FIX: Chamada de exportação corrigida (não precisa mais do companyId)
            LogHelper.exportarLogsGerenciamentoUsuarioXlsx(context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
