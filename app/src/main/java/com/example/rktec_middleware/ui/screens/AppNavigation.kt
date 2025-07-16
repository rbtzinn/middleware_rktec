// ui/screens/AppNavigation.kt
package com.example.rktec_middleware.ui.screens

sealed class Screen(val route: String) {
    object Autenticacao : Screen("autenticacao")
    object Principal : Screen("principal")
    object Importacao : Screen("importacao")
    object Inventario : Screen("inventario")
    object LeituraInventario : Screen("leitura_inventario")
    object ColetaAvulsa : Screen("coleta_avulsa")
    object Debug : Screen("debug")
    object Sobre : Screen("sobre")
    object GerenciamentoUsuarios : Screen("usuarios")
}