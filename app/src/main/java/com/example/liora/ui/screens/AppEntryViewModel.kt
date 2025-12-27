package com.example.liora.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Estados possíveis para a verificação inicial do aplicativo
sealed class AppEntryUiState {
    object Loading : AppEntryUiState() // Estado inicial enquanto o aplicativo está verificando
    object AuthenticatedAndProfileExists : AppEntryUiState() // O usuário está autenticado e o perfil completo existe no Firestore
    object Unauthenticated : AppEntryUiState() // O usuário não está autenticado (precisa fazer login)
    object AuthenticatedButNoProfile : AppEntryUiState() // O usuário está autenticado, mas o perfil não foi preenchido no Firestore (precisa cadastrar)
    data class Error(val message: String) : AppEntryUiState() // Ocorreu um erro durante a verificação inicial
}

/**
 * ViewModel responsável por verificar o status inicial do usuário (autenticação e preenchimento de perfil)
 * e guiar a navegação do aplicativo a partir da SplashScreen.
 *
 * @param application A instância da Application, necessária para o AndroidViewModel.
 */
class AppEntryViewModel(application: Application) : AndroidViewModel(application) {

    // Instâncias do Firebase Authentication e Firestore
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // MutableStateFlow para expor o estado da UI para a SplashScreen
    private val _uiState = MutableStateFlow<AppEntryUiState>(AppEntryUiState.Loading)
    val uiState = _uiState.asStateFlow() // Versão somente leitura do estado

    init {
        // Inicia a verificação do status do usuário assim que o ViewModel é criado
        checkUserStatus()
    }

    /**
     * Verifica o status de autenticação do usuário e a existência de seu perfil no Firestore.
     * Atualiza o estado da UI (_uiState) com o resultado da verificação,
     * determinando para qual tela o usuário deve ser direcionado.
     */
    private fun checkUserStatus() {
        // Lança uma coroutine no contexto de I/O para operações de rede/banco de dados
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = auth.currentUser // Obtém o usuário atualmente autenticado no Firebase

            if (currentUser != null) {
                // Se há um usuário autenticado, verificar se o perfil já foi preenchido no Firestore
                try {
                    val docRef = firestore.collection("users").document(currentUser.uid)
                    val snapshot = docRef.get().await() // Aguarda a conclusão da operação de leitura do Firestore

                    if (snapshot.exists()) {
                        // O documento do perfil do usuário existe no Firestore
                        _uiState.value = AppEntryUiState.AuthenticatedAndProfileExists
                    } else {
                        // O documento do perfil do usuário NÃO existe no Firestore
                        _uiState.value = AppEntryUiState.AuthenticatedButNoProfile
                    }
                } catch (e: Exception) {
                    // Captura qualquer exceção que ocorra durante a verificação do Firestore
                    // e atualiza o estado para Error, fornecendo a mensagem de erro.
                    _uiState.value = AppEntryUiState.Error("Erro ao verificar perfil do usuário: ${e.localizedMessage}")
                }
            } else {
                // Se não há usuário autenticado, o estado é Unauthenticated
                _uiState.value = AppEntryUiState.Unauthenticated
            }
        }
    }

    /**
     * Reseta o estado da UI para 'Loading'.
     * Isso é útil após uma navegação para evitar que o estado anterior seja re-emitido
     * se a SplashScreen for de alguma forma re-composta (embora menos comum após popUpTo).
     */
    fun resetState() {
        _uiState.value = AppEntryUiState.Loading
    }
}
