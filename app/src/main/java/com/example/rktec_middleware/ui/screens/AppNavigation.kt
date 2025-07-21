package com.example.rktec_middleware.ui.screens

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

// Sealed class para definir todas as rotas de navegação do aplicativo.
// Esta é a versão unificada e correta para o projeto.
sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Autenticacao : Screen("autenticacao")
    object Principal : Screen("principal")
    object Importacao : Screen("importacao")
    object Inventario : Screen("inventario")
    object Checagem : Screen("checagem")
    object ColetaAvulsa : Screen("coleta_avulsa")
    object Sobre : Screen("sobre")
    object GerenciamentoUsuarios : Screen("gerenciamento_usuarios")

    // Rota que precisa de argumentos para funcionar
    object LeituraInventario : Screen(
        route = "leitura_inventario/{filtroLoja}/{filtroSetor}",
        arguments = listOf(
            navArgument("filtroLoja") { type = NavType.StringType; nullable = true },
            navArgument("filtroSetor") { type = NavType.StringType; nullable = true }
        )
    ) {
        // Função auxiliar para criar a rota com os argumentos preenchidos de forma segura
        fun createRoute(loja: String?, setor: String?) = "leitura_inventario/${loja ?: "null"}/${setor ?: "null"}"
    }
}
