package com.example.liora.ui.screens

import androidx.compose.foundation.BorderStroke
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
val lifestyleQuestions = listOf(
    Question(1, "1. Qual seu ritmo de vida?", listOf("Agitado, sempre em movimento", "Equilibrado, entre agito e paz", "Tranquilo, gosto de calmaria", "Depende da fase que estou vivendo")),
    Question(2, "2. Você se considera", listOf("Caseira, amo ficar no meu cantinho", "Aventureira, topo quase tudo", "Social, gosto de estar cercado(a) de gente", "Independente, curto estar comigo mesmo(a)")),
    Question(3, "3. Você costuma planejar seu dia ou vai vivendo?", listOf("Planejo quase tudo", "Tenho uma ideia, mas deixo espaço pra imprevistos", "Vou vivendo, deixo fluir", "Depende da vibe do dia")),
    Question(4, "4. Qual dessas opções mais te define?", listOf("Trabalho muito e mal paro", "Estudo bastante, mas sei curtir", "Busco equilíbrio entre trabalho e lazer", "Tô numa fase de transição e descobertas")),
    Question(5, "5. Onde você se sente mais você?", listOf("Em casa, no meu espaço", "Em viagens, conhecendo lugares novos", "Em eventos e festas com amigos", "Em contato com a natureza ou coisas simples"))
)

/**
 * Tela de perguntas "Sobre Você", agora desacoplada do ViewModel.
 */
@Composable
fun SobreScreen(
    selectedAnswers: Map<Int, List<String>>,
    onAnswerChange: (questionId: Int, newAnswers: List<String>) -> Unit,
    onNavigateToNext: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            AppTopBar(title = "SOBRE VOCÊ", onBackClick = { /* TODO: Ação de voltar */ })
        },
        bottomBar = {
            PrimaryButton(
                text = "Próximo",
                onClick = onNavigateToNext,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = lifestyleQuestions.all { question -> selectedAnswers.containsKey(question.id) }
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
            items(lifestyleQuestions) { question ->
                SobreQuestionCard(
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
// COMPONENTES REUTILIZÁVEIS - INCLUÍDOS NO ARQUIVO PARA EVITAR ERROS
// =============================================================================

@Composable
fun SobreQuestionCard(
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
                SobreSelectableOptionRow(
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
fun SobreSelectableOptionRow(
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

// O PrimaryButton e AppTopBar devem estar em arquivos reutilizáveis para evitar redeclaração.
// Se eles já estiverem em um arquivo 'components', você pode apagar estas definições daqui.
// Por segurança, estão incluídos aqui para garantir que tudo compile.
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentPrimary,
            disabledContainerColor = ChipBackground
        ),
        enabled = enabled
    ) {
        Text(text = text, color = AccentText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SobreScreenPreview() {
    LioraTheme {
        // O Preview agora funciona perfeitamente, pois simula os dados.
        var dummyAnswers by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }
        SobreScreen(
            selectedAnswers = dummyAnswers,
            onAnswerChange = { qId, newAnswers -> dummyAnswers = dummyAnswers + (qId to newAnswers) },
            onNavigateToNext = {}
        )
    }
}
