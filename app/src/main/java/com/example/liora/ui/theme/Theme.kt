package com.example.liora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color // Importar Color
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

// Defina seus tons de preto e branco/cinza para o texto
val Black = Color(0xFF000000) // Preto absoluto
val DarkGray = Color(0xFF121212) // Um preto um pouco mais suave para superfícies, se desejar
val White = Color(0xFFFFFFFF) // Branco para texto
val LightGray = Color(0xFFB0B0B0) // Um cinza claro para texto secundário

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    onPrimary = White, // Cor do texto/ícones sobre a cor primária
    primaryContainer = DarkGray, // Cor de contêineres primários, como o AppBar
    onPrimaryContainer = White,
    secondary = Black,
    onSecondary = White,
    secondaryContainer = DarkGray,
    onSecondaryContainer = White,
    tertiary = Black,
    onTertiary = White,
    tertiaryContainer = DarkGray,
    onTertiaryContainer = White,
    background = Black, // Fundo principal da tela
    onBackground = White, // Cor do texto/ícones sobre o fundo
    surface = DarkGray, // Cor de superfícies como Cards, Sheets, Dialogs
    onSurface = White, // Cor do texto/ícones sobre superfícies
    surfaceVariant = DarkGray, // Variação de superfície
    onSurfaceVariant = LightGray,
    inverseSurface = White, // Usado para elementos que precisam contrastar com a superfície escura
    inverseOnSurface = Black,
    error = Color(0xFFCF6679), // Exemplo de cor de erro (pode ajustar)
    onError = Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = White,
    outline = Color(0xFF424242), // Cor de bordas, divisores
    outlineVariant = Color(0xFF2C2C2C),
    scrim = Color(0x99000000) // Usado para sobreposição de tela cheia
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = DarkGray,
    onPrimaryContainer = White,
    secondary = Black,
    onSecondary = White,
    secondaryContainer = DarkGray,
    onSecondaryContainer = White,
    tertiary = Black,
    onTertiary = White,
    tertiaryContainer = DarkGray,
    onTertiaryContainer = White,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    inverseSurface = White,
    inverseOnSurface = Black,
    error = Color(0xFFCF6679),
    onError = Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = White,
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF2C2C2C),
    scrim = Color(0x99000000)
)

@Composable
fun LioraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Se você quer que ambos os temas sejam *sempre* pretos,
        // o dynamicColor e o isSystemInDarkTheme() se tornam menos relevantes para a escolha das cores.
        // No entanto, para compatibilidade e se você decidir ter alguma variação no futuro, mantenha a lógica.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Mesmo com dynamicColor, vamos forçar cores pretas se for o desejo
            // Você pode comentar a linha abaixo e usar apenas o DarkColorScheme se quiser total controle
            if (darkTheme) dynamicDarkColorScheme(context).copy(
                primary = Black,
                onPrimary = White,
                background = Black,
                onBackground = White,
                surface = DarkGray,
                onSurface = White
            ) else dynamicLightColorScheme(context).copy(
                primary = Black,
                onPrimary = White,
                background = Black,
                onBackground = White,
                surface = DarkGray,
                onSurface = White
            )
        }
        // Se dynamicColor for false ou a versão do Android for menor que S
        darkTheme -> DarkColorScheme // Usa nosso esquema de cores escuras (totalmente preto)
        else -> LightColorScheme // Usa nosso esquema de cores claras (também totalmente preto)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Definir cor da barra de status (StatusBar)
            window.statusBarColor = colorScheme.background.toArgb() // Use a cor de fundo do seu tema (preto)
            // Definir cor da barra de navegação (NavigationBar)
            window.navigationBarColor = colorScheme.background.toArgb() // Use a cor de fundo do seu tema (preto)

            // Ajustar o ícone da barra de status para cores claras (se o fundo for escuro)
            // Isso garante que os ícones (como hora, bateria) sejam visíveis em um fundo preto
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Certifique-se de que sua tipografia esteja definida e com cores apropriadas para fundo escuro
        content = content
    )
}