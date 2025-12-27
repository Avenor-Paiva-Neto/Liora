package com.example.liora.domain.model

// MatchPreferences data class continua a mesma, sem alterações.
data class MatchPreferences(
    val maxDistance: Int = 100,
    val minAge: Int = 18,
    val maxAge: Int = 70,
    val lookingFor: String = "Todos",
    val wants: String = "Não sei"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "maxDistance" to maxDistance, "minAge" to minAge,
            "maxAge" to maxAge, "lookingFor" to lookingFor, "wants" to wants
        )
    }
}

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val birthYear: Int = 0,
    val locationName: String = "",
    val sexuality: String = "",
    val phoneNumber: String = "",
    val bio: String = "",


    val imageUrls: List<String> = emptyList(),
    val psycheProfile: PsycheProfile = PsycheProfile(),
    val preferences: MatchPreferences = MatchPreferences(),
    val subscriptionTier: String = "FREE",
    val undoState: Map<String, Any> = emptyMap()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "birthYear" to birthYear,
            "locationName" to locationName,
            "sexuality" to sexuality,
            "phoneNumber" to phoneNumber,
            "bio" to bio,

            "imageUrls" to imageUrls,
            "preferences" to preferences.toMap(),
            "psycheProfile" to psycheProfile.toMap(),
            "subscriptionTier" to subscriptionTier,
            "undoState" to undoState
        )
    }
}