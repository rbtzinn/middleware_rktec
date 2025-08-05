package com.example.rktec_middleware.ui.screens

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Autenticacao : Screen("autenticacao")
    object Principal : Screen("principal")
    object Importacao : Screen("importacao")
    object Configuracoes : Screen("configuracoes")
    object Inventario : Screen("inventario")
    object Checagem : Screen("checagem")
    object ColetaAvulsa : Screen("coleta_avulsa")
    object Sobre : Screen("sobre")
    object GerenciamentoUsuarios : Screen("gerenciamento_usuarios")
    object Historico : Screen("historico")
    object Perfil : Screen("perfil")

    // NOVA ROTA ADICIONADA AQUI
    object AguardandoProcessamento : Screen(
        route = "aguardando_processamento/{companyId}",
        arguments = listOf(navArgument("companyId") { type = NavType.StringType })
    ) {
        fun createRoute(companyId: String) = "aguardando_processamento/$companyId"
    }

    object DetalheHistorico : Screen(
        route = "detalhe_historico/{sessaoId}",
        arguments = listOf(navArgument("sessaoId") { type = NavType.LongType })
    ) {
        fun createRoute(sessaoId: Long) = "detalhe_historico/$sessaoId"
    }

    object LeituraInventario : Screen(
        route = "leitura_inventario/{filtroLoja}/{filtroSetor}",
        arguments = listOf(
            navArgument("filtroLoja") { type = NavType.StringType; nullable = true },
            navArgument("filtroSetor") { type = NavType.StringType; nullable = true }
        )
    ) {
        fun createRoute(loja: String?, setor: String?) = "leitura_inventario/${loja ?: "null"}/${setor ?: "null"}"
    }
}