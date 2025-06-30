package com.example.rktec_middleware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rktec_middleware.ui.theme.MiddlewareTheme
import com.example.rktec_middleware.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiddlewareTheme {
                App()
            }
        }
    }
}
