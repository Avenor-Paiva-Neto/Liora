package com.example.liora.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liora.Question // Importando o modelo de dados central
import com.example.liora.ui.theme.*

// A lista de perguntas para esta tela específica.
val personalityAndEmotionQuestions = listOf(
    Question(12, "1. Você costuma se abrir com facilidade?", listOf("Sim, sou bem transparente", "Depende da conexão", "Não muito, levo tempo", "Raramente, sou bem reservado(a)")),
    Question(13, "2. Como você reage diante de conflitos ou desentendimentos?", listOf("Prefiro resolver na hora, com diálogo", "Me afasto um pouco e penso antes", "Fico na minha, evito conflito ao máximo", "Depende muito da pessoa e da situação")),
    Question(14, "3. Como é seu senso de humor?", listOf("Sarcástico/irônico", "Bobo e leve", "Inteligente e provocador", "Tímido, mas presente", "Não sou muito de piadas")),
    Question(15, "4. Quando sente algo por alguém, você costuma…", listOf("Demonstrar sem medo", "Mostrar aos poucos", "Esperar sinais da outra pessoa", "Guardar pra mim")),
    Question(16, "5. Qual dessas frases mais combina com você?", listOf("Sinto tudo com muita intensidade", "Eu observo mais do que falo", "Sou do tipo coração aberto", "Sou mais razão que emoção", "Tenho meu próprio jeito de sentir"))
)

/**
 * Tela de perguntas "Personalidade & Emoção", desacoplada do ViewModel.
 */
@Composable
fun PersonalidadeScreen(
    selectedAnswers: Map<Int, List<String>>,
    onAnswerChange: (questionId: Int, newAnswers: List<String>) -> Unit,
    onNavigateToNext: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            AppTopBar(title = "PERSONALIDADE", onBackClick = { /* TODO */ })
        },
        bottomBar = {
            PrimaryButton(
                text = "Próximo",
                onClick = onNavigateToNext,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = personalityAndEmotionQuestions.all { question -> selectedAnswers.containsKey(question.id) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(personalityAndEmotionQuestions) { question ->
                PersonalidadeQuestionCard(
                    question = question,
                    selectedOption = selectedAnswers[question.id]?.firstOrNull(),
                    onOptionSelected = { selectedOption ->
                        onAnswerChange(question.id, listOf(selectedOption))
                    }
                )
            }
        }
    }
}


// =============================================================================
// COMPONENTES DESTA TELA - Mantendo a estrutura autocontida
// =============================================================================

@Composable
fun PersonalidadeQuestionCard(
    question: Question,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = question.text, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            question.options.forEach { optionText ->
                PersonalidadeSelectableOptionRow(
                    text = optionText,
                    isSelected = optionText == selectedOption,
                    onClick = { onOptionSelected(optionText) }
                )
                if (question.options.last() != optionText) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PersonalidadeSelectableOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val borderColor = if (isSelected) AccentPrimary else TextSecondary
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(BorderStroke(2.dp, borderColor), CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = TextPrimary, fontSize = 16.sp)
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PersonalidadeScreenPreview() {
    LioraTheme {
        // O Preview simula os dados para funcionar sem um ViewModel real.
        var dummyAnswers by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }
        PersonalidadeScreen(
            selectedAnswers = dummyAnswers,
            onAnswerChange = { qId, newAnswers -> dummyAnswers = dummyAnswers + (qId to newAnswers) },
            onNavigateToNext = {}
        )
    }
}
