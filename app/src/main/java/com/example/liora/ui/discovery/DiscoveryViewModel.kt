package com.example.liora.ui.discovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liora.domain.model.ImageDisplayInfo
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.UserRepository
import com.example.liora.domain.usecase.FindMatchesUseCase
import com.example.liora.domain.usecase.ProcessLikeActionUseCase
import com.example.liora.domain.usecase.ProcessSkipActionUseCase
import com.example.liora.domain.usecase.UndoLastSkipUseCase
import com.example.liora.ui.conversations.ConversationListItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DiscoveryEvent {
    data class ShowToast(val message: String) : DiscoveryEvent()
}

class DiscoveryViewModel(
    private val findMatchesUseCase: FindMatchesUseCase,
    private val processLikeActionUseCase: ProcessLikeActionUseCase,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val processSkipActionUseCase: ProcessSkipActionUseCase,
    private val undoLastSkipUseCase: UndoLastSkipUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val _matchState = MutableStateFlow<UserProfile?>(null)
    val matchState: StateFlow<UserProfile?> = _matchState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)

    private val _eventFlow = MutableSharedFlow<DiscoveryEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = DiscoveryUiState.Loading

            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = DiscoveryUiState.Error("Usuário não autenticado.")
                return@launch
            }

            userRepository.getUserData(userId)
                .onSuccess { profile ->
                    if (profile == null) {
                        _uiState.value = DiscoveryUiState.Error("Não foi possível carregar seu perfil.")
                        return@onSuccess
                    }
                    _currentUserProfile.value = profile
                    findMatches(profile)
                }
                .onFailure { error ->
                    _uiState.value = DiscoveryUiState.Error(error.message ?: "Falha ao carregar dados.")
                }
        }
    }

    private fun findMatches(currentUserProfile: UserProfile) {
        viewModelScope.launch {
            val result = findMatchesUseCase(currentUserProfile)
            result.onSuccess { profiles ->
                val discoveryListItems = profiles.map { userProfile ->
                    val imageUrl = userProfile.imageUrls.firstOrNull()
                    val imageDisplayInfo = ImageDisplayInfo(imageUrl, shouldBlur = true)
                    ConversationListItem(userProfile, imageDisplayInfo)
                }
                _uiState.value = DiscoveryUiState.Success(discoveryListItems)
            }.onFailure { error ->
                _uiState.value = DiscoveryUiState.Error(error.message ?: "Erro ao buscar perfis.")
            }
        }
    }

    fun onAction(action: DiscoveryAction) {
        viewModelScope.launch {
            val currentListItem = (_uiState.value as? DiscoveryUiState.Success)?.profiles?.firstOrNull()
            val currentProfile = currentListItem?.userProfile

            // A verificação de perfil nulo não se aplica a Undo e Retry.
            if (currentProfile == null && action !is DiscoveryAction.Undo && action !is DiscoveryAction.Retry) {
                _eventFlow.emit(DiscoveryEvent.ShowToast("Nenhum perfil disponível para esta ação."))
                return@launch
            }

            when (action) {
                is DiscoveryAction.Like -> {
                    currentProfile?.let { profile ->
                        removeProfileFromUi(profile)
                        val result = processLikeActionUseCase(profile)
                        result.onSuccess { matchResult ->
                            if (matchResult.isMatch) {
                                _matchState.value = matchResult.matchedProfile
                            }
                        }.onFailure { error ->
                            Log.e("LikeError", "Falha ao processar like: ${error.message}", error)
                        }
                    }
                }
                is DiscoveryAction.Skip -> {
                    currentProfile?.let { profile ->
                        removeProfileFromUi(profile)
                        processSkipActionUseCase(profile).onFailure { error ->
                            Log.e("SkipError", "Falha ao processar skip: ${error.message}", error)
                        }
                    }
                }
                is DiscoveryAction.Undo -> {
                    val result = undoLastSkipUseCase()
                    result.onSuccess { restoredProfile ->
                        if (restoredProfile != null) {
                            val restoredListItem = ConversationListItem(
                                userProfile = restoredProfile,
                                imageDisplayInfo = ImageDisplayInfo(restoredProfile.imageUrls.firstOrNull(), shouldBlur = true)
                            )
                            addProfileToUi(restoredListItem)
                            _eventFlow.emit(DiscoveryEvent.ShowToast("Perfil anterior restaurado!"))
                        } else {
                            _eventFlow.emit(DiscoveryEvent.ShowToast("Nenhuma ação para desfazer."))
                        }
                    }.onFailure { error ->
                        _eventFlow.emit(DiscoveryEvent.ShowToast(error.message ?: "Não foi possível desfazer."))
                    }
                }
                is DiscoveryAction.Report -> {
                    _eventFlow.emit(DiscoveryEvent.ShowToast("Ação de Denunciar (Report) ainda não implementada."))
                }
                is DiscoveryAction.Chat -> {
                    _eventFlow.emit(DiscoveryEvent.ShowToast("Ação de Chat ainda não implementada."))
                }
                is DiscoveryAction.BioToggle -> {
                    // Ação tratada na UI
                }
                // NOVO CASE ADICIONADO AQUI
                is DiscoveryAction.Retry -> {
                    _currentUserProfile.value?.let { profile ->
                        findMatches(profile)
                    } ?: run {
                        // Fallback caso o perfil do usuário não tenha sido carregado por algum motivo
                        loadInitialData()
                    }
                }
            }
        }
    }

    fun clearMatchState() {
        _matchState.value = null
    }

    fun getCurrentUserProfile(): UserProfile? {
        return _currentUserProfile.value
    }

    private fun removeProfileFromUi(profileToRemove: UserProfile) {
        if (_uiState.value is DiscoveryUiState.Success) {
            _uiState.update { currentState ->
                val currentListItems = (currentState as DiscoveryUiState.Success).profiles
                val updatedListItems = currentListItems.filterNot { it.userProfile.uid == profileToRemove.uid }
                DiscoveryUiState.Success(updatedListItems)
            }
        }
    }

    private fun addProfileToUi(listItemToAdd: ConversationListItem) {
        if (_uiState.value is DiscoveryUiState.Success) {
            _uiState.update { currentState ->
                val currentListItems = (currentState as DiscoveryUiState.Success).profiles
                val updatedListItems = listOf(listItemToAdd) + currentListItems
                DiscoveryUiState.Success(updatedListItems)
            }
        }
    }
}