// Arquivo: settings.gradle.kts

pluginManagement {
    repositories {
        // ===================================================================
        // MUDANÇA AQUI: Simplificamos o bloco do Google para garantir
        // que todos os plugins necessários sejam encontrados.
        google()
        // ===================================================================
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Liora"
include(":app")