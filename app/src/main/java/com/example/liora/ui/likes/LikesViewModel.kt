package com.example.liora.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.UserRepository
import com.example.liora.domain.usecase.DetermineImageBlurStatusUseCase
import com.example.liora.domain.usecase.GetLikerProfilesUseCase
import com.example.liora.domain.usecase.ProcessLikeActionUseCase
import com.example.liora.ui.conversations.ConversationListItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LikesUiState {
    object Loading : LikesUiState()
    data class Success(val profiles: List<ConversationListItem>) : LikesUiState()
    object Empty : LikesUiState()
    data class Error(val message: String) : LikesUiState()
}

class LikesViewModel(
    private val getLikerProfilesUseCase: GetLikerProfilesUseCase,
    private val processLikeActionUseCase: ProcessLikeActionUseCase,
    private val determineImageBlurStatusUseCase: DetermineImageBlurStatusUseCase,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<LikesUiState>(LikesUiState.Loading)
    val uiState: StateFlow<LikesUiState> = _uiState.asStateFlow()

    private val _newMatchState = MutableStateFlow<UserProfile?>(null)
    val newMatchState: StateFlow<UserProfile?> = _newMatchState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                userRepository.getUserData(userId).onSuccess { profile ->
                    _currentUserProfile.value = profile
                }
            }
            loadLikerProfiles()
        }
    }

    fun loadLikerProfiles() {
        viewModelScope.launch {
            _uiState.value = LikesUiState.Loading
            val result = getLikerProfilesUseCase()
            result.onSuccess { userProfiles ->
                val conversationListItems = userProfiles.map { userProfile ->
                    val imageUrl = userProfile.imageUrls.firstOrNull()
                    val imageDisplayInfo = com.example.liora.domain.model.ImageDisplayInfo(imageUrl, shouldBlur = true)
                    ConversationListItem(userProfile, imageDisplayInfo)
                }

                _uiState.value = if (conversationListItems.isEmpty()) {
                    LikesUiState.Empty
                } else {
                    LikesUiState.Success(conversationListItems)
                }
            }.onFailure { error ->
                _uiState.value = LikesUiState.Error(error.message ?: "Erro desconhecido.")
            }
        }
    }

    fun onLikeClicked(likedProfile: UserProfile) {
        viewModelScope.launch {
            val result = processLikeActionUseCase(likedProfile)

            result.onSuccess { matchResult ->
                if (matchResult.isMatch) {
                    _newMatchState.value = matchResult.matchedProfile
                }
            }

            removeProfileFromState(likedProfile)
        }
    }

    fun onDismissClicked(dismissedProfile: UserProfile) {
        viewModelScope.launch {
            // TODO: Implementar a lógica de "dislike" no backend.
            removeProfileFromState(dismissedProfile)
        }
    }

    fun clearMatchState() {
        _newMatchState.value = null
    }

    private fun removeProfileFromState(profileToRemove: UserProfile) {
        val currentState = _uiState.value
        if (currentState is LikesUiState.Success) {
            val updatedProfiles = currentState.profiles.filterNot { it.userProfile.uid == profileToRemove.uid }

            _uiState.update {
                if (updatedProfiles.isEmpty()) {
                    LikesUiState.Empty
                } else {
                    LikesUiState.Success(updatedProfiles)
                }
            }
        }
    }
}