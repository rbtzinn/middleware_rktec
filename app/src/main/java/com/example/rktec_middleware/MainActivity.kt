package com.example.rktec_middleware

import android.os.Bundle
import android.view.KeyEvent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
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
import com.example.rktec_middleware.ui.screens.TelaDebug
import com.example.rktec_middleware.ui.screens.TelaMapeamentoPlanilha
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
            var telaAtual by remember { mutableStateOf("menu") }
            var uriParaMapeamento by remember { mutableStateOf<Uri?>(null) }
            var refreshInventario by remember { mutableStateOf(0) }
            val scope = rememberCoroutineScope()


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
                    onIniciarLeituraInventario = { telaAtual = "leituraInventario" }
                )

                "mapeamento" -> {
                    uriParaMapeamento?.let { uri ->
                        TelaMapeamentoPlanilha(
                            uri = uri,
                            onSalvar = { mapeamento ->
                                // Agora usa scope.launch!
                                scope.launch {
                                    appDatabase.mapeamentoDao().inserir(mapeamento)
                                    telaAtual = "inventario"
                                    refreshInventario++
                                    uriParaMapeamento = null
                                }
                            },
                            onCancelar = {
                                telaAtual = "inventario"
                                uriParaMapeamento = null
                            }
                        )
                    }
                }
                "leituraInventario" -> TelaLeituraInventario(
                    onVoltar = { telaAtual = "menu" },
                    banco = appDatabase
                )
                "debug" -> TelaDebug(
                    banco = appDatabase,
                    onVoltar = { telaAtual = "menu" }
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
