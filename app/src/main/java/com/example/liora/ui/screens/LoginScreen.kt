package com.example.liora.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.liora.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToCadastro: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by loginViewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        loginViewModel.handleGoogleSignInResult(result.data)
    }

    // Lida com os estados de navegação e feedback
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.NavigateToDiscovery -> {
                onNavigateToDiscovery()
                loginViewModel.resetState()
            }
            is LoginUiState.NavigateToCadastro -> {
                onNavigateToCadastro()
                loginViewModel.resetState()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                loginViewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text("INSIRA SEU GRÁFICO AQUI", color = TextSecondary)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vamos criar\nsua conta",
                    color = TextPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    lineHeight = 48.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { loginViewModel.startGoogleSignIn(launcher) },
                        enabled = loginState !is LoginUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = LightButtonBackground)
                    ) {
                        Text(
                            text = "LOGAR COM GOOGLE",
                            color = LightButtonText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        SocialLoginIcon(onClick = { /* TODO */ }) {
                            Text("G", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                        SocialLoginIcon(onClick = { /* TODO */ }) {
                            Icon(Icons.Outlined.Person, "Login Genérico", tint = TextSecondary, modifier = Modifier.size(28.dp))
                        }
                        SocialLoginIcon(onClick = { /* TODO */ }) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, "Outro método", tint = TextSecondary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        LoadingNotificationCard(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = loginState is LoginUiState.Loading
        )
    }
}

@Composable
fun LoadingNotificationCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = ChipBackground)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Autenticando sua conta...",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SocialLoginIcon(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(67.dp)
            .clip(CircleShape)
            .background(ChipBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LioraTheme {
        LoginScreen(
            onNavigateToDiscovery = {},
            onNavigateToCadastro = {}
        )
    }
}
