package com.example.rktec_middleware

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.rktec_middleware.ui.theme.MiddlewareTheme
import com.example.rktec_middleware.ui.App
import com.example.rktec_middleware.viewmodel.RfidViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RfidViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiddlewareTheme {
                App(viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("RFID", "Key pressed: $keyCode") // Só para debug!
        if (keyCode == KeyEvent.KEYCODE_F1) { // Troque pelo keycode do seu leitor!
            viewModel.startReading()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1) { // Troque pelo keycode do seu leitor!
            viewModel.stopReading()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
