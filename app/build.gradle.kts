import java.util.Properties // <-- MUDANÇA AQUI: Import necessário

// <-- MUDANÇA AQUI: Código para carregar o arquivo local.properties de forma segura
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.liora"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.liora"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // <-- MUDANÇA AQUI: Bloco debug adicionado para a chave de API
        debug {
            buildConfigField(
                "String",
                "GOOGLE_WEB_CLIENT_ID",
                properties.getProperty("GOOGLE_WEB_CLIENT_ID", "\"\"")
            )
        }
        release {
            // <-- MUDANÇA AQUI: Habilitado para produção
            isMinifyEnabled = true
            isShrinkResources = true // <-- MUDANÇA AQUI: Adicionado para remover recursos não usados

            // <-- MUDANÇA AQUI: Adicionada a chave de API para a versão de release
            buildConfigField(
                "String",
                "GOOGLE_WEB_CLIENT_ID",
                properties.getProperty("GOOGLE_WEB_CLIENT_ID", "\"\"")
            )

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // MUDE AQUI
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // E MUDE AQUI
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        // <-- MUDANÇA AQUI: Habilitar o BuildConfig para que o código possa acessá-lo
        buildConfig = true
    }
    // O composeOptions foi removido pois as versões são gerenciadas pelo BOM (Bill of Materials)
}

dependencies {
    // --- Dependências Core e de UI (sem alterações) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.google.libphonenumber)
    implementation(libs.play.services.location)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.34.0")


}
