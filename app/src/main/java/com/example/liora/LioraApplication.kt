package com.example.liora

import android.app.Application
import com.example.liora.di.AppContainer

class LioraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa nosso container de dependências assim que o app é criado.
        AppContainer.init(this)
    }
}