package com.example.liora.ui.screens

import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import com.example.liora.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Estados possíveis da tela de login
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object NavigateToDiscovery : LoginUiState() // Usuário já tem perfil
    object NavigateToCadastro : LoginUiState()  // Usuário precisa fazer cadastro
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(getApplication(), googleSignInOptions)

    /**
     * Inicia o processo de login com Google.
     */
    fun startGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    /**
     * Trata o resultado do login com o Google.
     */
    fun handleGoogleSignInResult(intent: Intent?) {
        _uiState.value = LoginUiState.Loading
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)

        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            _uiState.value = LoginUiState.Error("Falha no login com Google. (Código: ${e.statusCode})")
        }
    }

    /**
     * Autentica com Firebase usando a conta do Google.
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        checkUserProfileExists(uid)
                    } else {
                        _uiState.value = LoginUiState.Error("Erro ao obter UID do usuário.")
                    }
                } else {
                    _uiState.value = LoginUiState.Error(task.exception?.message ?: "Erro desconhecido ao autenticar.")
                }
            }
    }

    /**
     * Verifica no Firestore se o usuário já tem um perfil completo.
     */
    private fun checkUserProfileExists(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docRef = firestore.collection("users").document(uid)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    _uiState.value = LoginUiState.NavigateToDiscovery
                } else {
                    _uiState.value = LoginUiState.NavigateToCadastro
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Erro ao verificar perfil: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Reseta o estado da UI.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
