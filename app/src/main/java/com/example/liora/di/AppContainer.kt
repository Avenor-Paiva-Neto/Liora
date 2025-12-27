package com.example.liora.di

import android.app.Application
import com.example.liora.data.repository.AndroidLocationRepositoryImpl
import com.example.liora.data.repository.FirebaseChatRepositoryImpl
import com.example.liora.data.repository.FirebaseInteractionRepositoryImpl
import com.example.liora.data.repository.FirebasePresenceRepositoryImpl
import com.example.liora.data.repository.FirebaseStorageRepositoryImpl
import com.example.liora.data.repository.FirebaseUserRepositoryImpl
import com.example.liora.domain.compatibility.CompatibilityScorer
import com.example.liora.domain.repository.ChatRepository
import com.example.liora.domain.repository.InteractionRepository
import com.example.liora.domain.repository.LocationRepository
import com.example.liora.domain.repository.PresenceRepository
import com.example.liora.domain.repository.StorageRepository
import com.example.liora.domain.repository.UserRepository
import com.example.liora.domain.usecase.* // Importa todos os UseCases
import com.example.liora.domain.utils.PhoneNumberValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database

object AppContainer {
    private lateinit var application: Application
    fun init(app: Application) { application = app }

    // --- SERVIÇOS DO FIREBASE ---
    val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }
    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }
    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val realtimeDatabase: FirebaseDatabase by lazy { Firebase.database }

    // --- REPOSITÓRIOS ---
    val locationRepository: LocationRepository by lazy { AndroidLocationRepositoryImpl(application) }
    val userRepository: UserRepository by lazy { FirebaseUserRepositoryImpl(firestore) }
    // MODIFICAÇÃO AQUI: Passando 'application' (Contexto) para o FirebaseStorageRepositoryImpl
    val storageRepository: StorageRepository by lazy { FirebaseStorageRepositoryImpl(storage, application) }
    val interactionRepository: InteractionRepository by lazy { FirebaseInteractionRepositoryImpl(firestore) }
    val chatRepository: ChatRepository by lazy { FirebaseChatRepositoryImpl(firestore) }
    val presenceRepository: PresenceRepository by lazy { FirebasePresenceRepositoryImpl(realtimeDatabase) }

    // --- UTILITÁRIOS E ESPECIALISTAS DE DOMÍNIO ---
    val phoneNumberValidator: PhoneNumberValidator by lazy { PhoneNumberValidator() }
    val compatibilityScorer: CompatibilityScorer by lazy { CompatibilityScorer }

    // --- CASOS DE USO (USE CASES) ---
    val mapAnswersUseCase: MapAnswersToPsycheProfileUseCase by lazy { MapAnswersToPsycheProfileUseCase() }
    val createUserProfileUseCase: CreateUserProfileUseCase by lazy { CreateUserProfileUseCase(userRepository, storageRepository, firebaseAuth) }

    val findMatchesUseCase: FindMatchesUseCase by lazy {
        FindMatchesUseCase(userRepository, compatibilityScorer, interactionRepository)
    }

    val processLikeActionUseCase: ProcessLikeActionUseCase by lazy {
        ProcessLikeActionUseCase(interactionRepository, firebaseAuth)
    }

    val getLikerProfilesUseCase: GetLikerProfilesUseCase by lazy {
        GetLikerProfilesUseCase(interactionRepository, firebaseAuth)
    }

    val processSkipActionUseCase: ProcessSkipActionUseCase by lazy {
        ProcessSkipActionUseCase(interactionRepository, userRepository, firebaseAuth)
    }

    val undoLastSkipUseCase: UndoLastSkipUseCase by lazy {
        UndoLastSkipUseCase(userRepository, interactionRepository, firebaseAuth)
    }

    // --- USE CASES PARA CHAT ---
    val sendMessageUseCase: SendMessageUseCase by lazy { SendMessageUseCase(chatRepository) }
    val observeMessagesUseCase: ObserveMessagesUseCase by lazy { ObserveMessagesUseCase(chatRepository) }
    val updateMessageStatusUseCase: UpdateMessageStatusUseCase by lazy { UpdateMessageStatusUseCase(chatRepository) }
    val updatePresenceUseCase: UpdatePresenceUseCase by lazy { UpdatePresenceUseCase(presenceRepository) }
    val observePresenceUseCase: ObservePresenceUseCase by lazy { ObservePresenceUseCase(presenceRepository) }
    val updateTypingStatusUseCase: UpdateTypingStatusUseCase by lazy { UpdateTypingStatusUseCase(presenceRepository) }
    val observeTypingStatusUseCase: ObserveTypingStatusUseCase by lazy { ObserveTypingStatusUseCase(presenceRepository) }
    val ensureChatDocumentUseCase: EnsureChatDocumentUseCase by lazy { EnsureChatDocumentUseCase(chatRepository) }

    // USE CASE PARA CONVERSAS
    val observeMatchedUsersUseCase: ObserveMatchedUsersUseCase by lazy {
        ObserveMatchedUsersUseCase(interactionRepository, userRepository)
    }

    // NOVO: USE CASE PARA LÓGICA DE BLUR
    val determineImageBlurStatusUseCase: DetermineImageBlurStatusUseCase by lazy { // ADICIONADO AQUI
        DetermineImageBlurStatusUseCase(chatRepository) // Ele precisa do chatRepository
    }
}