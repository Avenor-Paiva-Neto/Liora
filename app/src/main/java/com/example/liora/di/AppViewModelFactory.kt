package com.example.liora.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.liora.ui.chat.ChatViewModel
import com.example.liora.ui.conversations.ConversationsViewModel
import com.example.liora.ui.discovery.DiscoveryViewModel
import com.example.liora.ui.likes.LikesViewModel
import com.example.liora.ui.onboarding.OnboardingViewModel
import com.example.liora.ui.screens.AppEntryViewModel
import com.example.liora.ui.screens.LoginViewModel

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val viewModel = when {

            modelClass.isAssignableFrom(AppEntryViewModel::class.java) -> {
                AppEntryViewModel(application = application)
            }

            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(
                    createUserProfileUseCase = AppContainer.createUserProfileUseCase,
                    validator = AppContainer.phoneNumberValidator,
                    locationRepository = AppContainer.locationRepository,
                    mapAnswersUseCase = AppContainer.mapAnswersUseCase
                )
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(application = application)
            }

            modelClass.isAssignableFrom(DiscoveryViewModel::class.java) -> {
                DiscoveryViewModel(
                    findMatchesUseCase = AppContainer.findMatchesUseCase,
                    processLikeActionUseCase = AppContainer.processLikeActionUseCase,
                    userRepository = AppContainer.userRepository,
                    auth = AppContainer.firebaseAuth,
                    processSkipActionUseCase = AppContainer.processSkipActionUseCase,
                    undoLastSkipUseCase = AppContainer.undoLastSkipUseCase
                )
            }

            // **MUDANÇA PRINCIPAL AQUI**
            // Agora injetamos o UserRepository e o FirebaseAuth no LikesViewModel.
            modelClass.isAssignableFrom(LikesViewModel::class.java) -> {
                LikesViewModel(
                    getLikerProfilesUseCase = AppContainer.getLikerProfilesUseCase,
                    processLikeActionUseCase = AppContainer.processLikeActionUseCase,
                    determineImageBlurStatusUseCase = AppContainer.determineImageBlurStatusUseCase,
                    // Novas dependências adicionadas:
                    userRepository = AppContainer.userRepository,
                    auth = AppContainer.firebaseAuth
                )
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                val savedStateHandle = extras.createSavedStateHandle()
                ChatViewModel(
                    savedStateHandle = savedStateHandle,
                    sendMessageUseCase = AppContainer.sendMessageUseCase,
                    observeMessagesUseCase = AppContainer.observeMessagesUseCase,
                    updateMessageStatusUseCase = AppContainer.updateMessageStatusUseCase,
                    updatePresenceUseCase = AppContainer.updatePresenceUseCase,
                    observePresenceUseCase = AppContainer.observePresenceUseCase,
                    updateTypingStatusUseCase = AppContainer.updateTypingStatusUseCase,
                    observeTypingStatusUseCase = AppContainer.observeTypingStatusUseCase,
                    userRepository = AppContainer.userRepository,
                    firebaseAuth = AppContainer.firebaseAuth,
                    ensureChatDocumentUseCase = AppContainer.ensureChatDocumentUseCase
                )
            }

            modelClass.isAssignableFrom(ConversationsViewModel::class.java) -> {
                ConversationsViewModel(
                    application = application,
                    observeMatchedUsersUseCase = AppContainer.observeMatchedUsersUseCase,
                    determineImageBlurStatusUseCase = AppContainer.determineImageBlurStatusUseCase,
                    firebaseAuth = AppContainer.firebaseAuth
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}