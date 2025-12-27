package com.example.liora.ui.screens

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.liora.di.AppViewModelFactory
import com.example.liora.ui.onboarding.OnboardingViewModel // <-- Nosso único ViewModel
import com.example.liora.ui.onboarding.SubmissionState
import com.example.liora.ui.theme.*
// A linha 'import com.example.liora.onboarding.OnboardingStateHolderViewModel' foi removida.

// =============================================================================
// ROTA INTELIGENTE (VERSÃO FINAL E SIMPLIFICADA)
// =============================================================================
@Composable
fun OnboardingBioRoute(
    // Ações de navegação, agora sem receber nenhum ViewModel como parâmetro.
    onSubmissionSuccess: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onBackClick: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    // 1. Instanciamos nosso ÚNICO e poderoso ViewModel com a fábrica.
    val viewModel: OnboardingViewModel = viewModel(factory = AppViewModelFactory(application))

    // 2. Observamos todos os estados que precisamos, vindos do mesmo ViewModel.
    val submissionState by viewModel.submissionState.collectAsState()
    val bioText by viewModel.bio.collectAsState()
    val termsChecked by viewModel.termsChecked.collectAsState()
    val context = LocalContext.current

    // Efeito para lidar com o resultado da submissão (continua igual).
    LaunchedEffect(submissionState) {
        when (val state = submissionState) {
            is SubmissionState.Success -> {
                Toast.makeText(context, "Perfil criado com sucesso!", Toast.LENGTH_SHORT).show()
                onSubmissionSuccess()
            }
            is SubmissionState.Error -> {
                Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetSubmissionState() // Nome da função corrigido
            }
            else -> { /* Não faz nada */ }
        }
    }

    // Box para a sobreposição de loading (continua igual).
    Box(modifier = Modifier.fillMaxSize()) {

        // 3. Chamamos a BioScreen, mas agora todas as ações e estados
        // vêm do nosso único 'viewModel'.
        BioScreen(
            bioText = bioText,
            onBioChange = { viewModel.onBioChange(it) },
            termsChecked = termsChecked,
            onTermsCheckedChange = { viewModel.onTermsCheckedChange(it) },
            onNavigateToTerms = onNavigateToTerms,
            onBackClick = onBackClick,
            onFinalize = {
                // A chamada de finalização agora é muito mais simples e direta!
                viewModel.finalizeOnboarding()
            }
        )

        if (submissionState is SubmissionState.Loading) {
            LoadingOverlay()
        }
    }
}

// =============================================================================
// O restante do arquivo (BioScreen, LoadingOverlay, etc.) não precisa de
// nenhuma alteração, pois já era bem estruturado.
// =============================================================================
@Composable
fun BioScreen(
    bioText: String, onBioChange: (String) -> Unit, termsChecked: Boolean,
    onTermsCheckedChange: (Boolean) -> Unit, onFinalize: () -> Unit,
    onNavigateToTerms: () -> Unit, onBackClick: () -> Unit
) {
    val maxBioLength = 300
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = { BioTopBar(onBackClick = onBackClick) },
        bottomBar = {
            BioFinalActionsSection(
                checked = termsChecked, onCheckedChange = onTermsCheckedChange,
                onFinalizeClick = onFinalize, isButtonEnabled = bioText.isNotBlank() && termsChecked,
                onTermsClick = onNavigateToTerms
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp)
            ) {
                TextField(
                    value = bioText, onValueChange = { if (it.length <= maxBioLength) onBioChange(it) },
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    placeholder = { Text("Fale sobre você sem medo", color = TextSecondary, fontSize = 16.sp) },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundSecondary, unfocusedContainerColor = BackgroundSecondary,
                        disabledContainerColor = BackgroundSecondary, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${bioText.length}/$maxBioLength", color = TextSecondary, fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(text = "Finalizando seu perfil...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun BioTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(BackgroundSecondary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 55.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "SUA BIOGRAFIA", color = TextPrimary, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Text(text = "Todos Iram ver, é seu passaporte", color = TextSecondary, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(AccentPrimary).clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = AccentText)
            }
        }
    }
}

@Composable
fun BioFinalActionsSection(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit, onFinalizeClick: () -> Unit,
    isButtonEnabled: Boolean, onTermsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = AccentPrimary, uncheckedColor = TextSecondary)
            )
            Spacer(Modifier.width(8.dp))
            BioAnnotatedTermsText(onTermsClick = onTermsClick)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onFinalizeClick, modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, disabledContainerColor = ChipBackground),
            enabled = isButtonEnabled
        ) {
            Text(text = "Finalizar", color = AccentText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun BioAnnotatedTermsText(onTermsClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("EU li e concordo com os termos de uso. ")
        pushStringAnnotation(tag = "TERMS", annotation = "terms_route")
        withStyle(style = SpanStyle(color = AccentPrimary, fontWeight = FontWeight.Bold)) {
            append("TERMOS DE USO")
        }
        pop()
    }
    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset).firstOrNull()
                ?.let { onTermsClick() }
        },
        style = TextStyle(color = TextPrimary, fontSize = 14.sp)
    )
}

@Preview(showBackground = true)
@Composable
fun BioScreenPreview() {
    LioraTheme {
        var dummyBioText by remember { mutableStateOf("Escrevendo minha história...") }
        var dummyTermsChecked by remember { mutableStateOf(true) }
        BioScreen(
            bioText = dummyBioText, onBioChange = { dummyBioText = it },
            termsChecked = dummyTermsChecked, onTermsCheckedChange = { dummyTermsChecked = it },
            onFinalize = {}, onNavigateToTerms = {}, onBackClick = {}
        )
    }
}