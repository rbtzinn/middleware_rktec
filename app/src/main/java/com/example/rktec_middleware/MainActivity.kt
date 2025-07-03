package com.example.rktec_middleware

import TelaImportacao
import android.os.Bundle
import android.view.KeyEvent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import com.example.rktec_middleware.ui.screens.TelaLeituraColeta
import com.example.rktec_middleware.viewmodel.RfidViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.rktec_middleware.viewmodel.RfidViewModelFactory
import com.example.rktec_middleware.ui.screens.TelaInventario
import com.example.rktec_middleware.ui.screens.TelaLeituraInventario
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.screens.TelaDebug
import com.example.rktec_middleware.ui.screens.TelaLogin
import com.example.rktec_middleware.ui.screens.TelaSobre
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RfidViewModel
    private var lendo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            RfidViewModelFactory(applicationContext)
        ).get(RfidViewModel::class.java)

        val appDatabase = AppDatabase.getInstance(applicationContext)


        setContent {
            // -------- ESTADOS DO CICLO DE TELAS --------
            var isLoggedIn by remember { mutableStateOf(false) }
            var mapeamentoConcluido by remember { mutableStateOf(false) }
            var telaAtual by remember { mutableStateOf("menu") }
            var uriParaMapeamento by remember { mutableStateOf<Uri?>(null) }
            val scope = rememberCoroutineScope()
            var refreshDebug by remember { mutableStateOf(0) }
            var usuario by remember { mutableStateOf("") }
            var filtroLoja by remember { mutableStateOf<String?>(null) }
            var filtroSetor by remember { mutableStateOf<String?>(null) }
            var listaTotal by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
            var listaFiltrada by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }


            if (!isLoggedIn) {
                TelaLogin(
                    onLoginSucesso = { nome ->
                        usuario = nome
                        isLoggedIn = true
                        scope.launch {
                            val mapeamento = appDatabase.mapeamentoDao().buscarPrimeiro()
                            mapeamentoConcluido = mapeamento != null
                        }
                    },
                    onSobreClick = { telaAtual = "sobre" }
                )
                return@setContent
            }


            if (!mapeamentoConcluido) {
                TelaImportacao(
                    onConcluido = {
                        mapeamentoConcluido = true
                        refreshDebug++
                        telaAtual = "menu"
                    },
                    appDatabase = appDatabase,
                    usuario = usuario,
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" }
                )

                return@setContent
            }




            when (telaAtual) {
                "menu" -> TelaPrincipal(
                    onColetaClick = { telaAtual = "leitura" },
                    onInventarioClick = { telaAtual = "inventario" },
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" }
                )
                "leitura" -> TelaLeituraColeta(
                    viewModel = viewModel,
                    onVoltar = { telaAtual = "menu" }
                )
                "inventario" -> TelaInventario(
                    onVoltar = { telaAtual = "menu" },
                    onIniciarLeituraInventario = { loja, setor, total, filtrada ->
                        filtroLoja = loja
                        filtroSetor = setor
                        listaTotal = total
                        listaFiltrada = filtrada
                        telaAtual = "leituraInventario"
                    },
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" }
                )

                "leituraInventario" -> TelaLeituraInventario(
                    onVoltar = { telaAtual = "menu" },
                    banco = appDatabase,
                    listaFiltrada = listaFiltrada,
                    listaTotal = listaTotal,
                    filtroLoja = filtroLoja,
                    filtroSetor = filtroSetor
                )
                "debug" -> TelaDebug(
                    banco = appDatabase,
                    refresh = refreshDebug,
                    onVoltar = { telaAtual = "menu" },
                    onBancoLimpo = {
                        mapeamentoConcluido = false
                        telaAtual = "menu"
                    }
                )

                "sobre" -> TelaSobre(
                    onVoltar = { telaAtual = "menu" }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && !lendo) {
            lendo = true
            viewModel.startReading()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && lendo) {
            lendo = false
            viewModel.stopReading()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
