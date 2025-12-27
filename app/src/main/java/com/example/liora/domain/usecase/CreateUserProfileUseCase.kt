package com.example.liora.domain.usecase

import android.net.Uri
import com.example.liora.domain.model.MatchPreferences
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.model.PsycheProfile
import com.example.liora.domain.repository.StorageRepository
import com.example.liora.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CreateUserProfileUseCase(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository,
    private val auth: FirebaseAuth
) {

    // A assinatura da função agora aceita todos os nossos novos dados.
    suspend operator fun invoke(
        name: String,
        birthYear: Int,
        locationName: String,
        sexuality: String,
        phoneNumber: String,
        bio: String,

        imageUris: List<Uri>,
        preferences: MatchPreferences,
        psycheProfile: PsycheProfile
    ): Result<Unit> {

        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(Exception("Nenhum usuário autenticado para criar o perfil."))

        return try {
            coroutineScope {
                // A parte do upload de imagens continua igual (desativada por enquanto)
                val uploadedImageUrls = imageUris.map { uri ->
                    async { storageRepository.uploadProfileImage(currentUserId, uri).getOrThrow() }
                }.map { it.await() }

                // Criamos o objeto de perfil completo com TODOS os dados.
                val userProfile = UserProfile(
                    uid = currentUserId,
                    name = name,
                    birthYear = birthYear,
                    locationName = locationName,
                    sexuality = sexuality,
                    phoneNumber = phoneNumber,
                    bio = bio,

                    imageUrls = uploadedImageUrls,
                    preferences = preferences,
                    psycheProfile = psycheProfile
                )

                // Salvamos o objeto final e completo no Firestore.
                userRepository.saveUserData(userProfile).getOrThrow()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}