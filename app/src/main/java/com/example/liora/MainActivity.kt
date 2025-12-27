package com.example.liora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.liora.ui.AppNavigation // Importa nosso mapa de navegação
import com.example.liora.ui.theme.LioraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LioraTheme {
                AppNavigation()
            }
        }
    }
}
