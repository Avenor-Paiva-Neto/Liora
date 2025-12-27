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
val limitsAndValuesQuestions = listOf(
    Question(17, "1. O que você considera inaceitável em alguém com quem se relaciona?", listOf("Desonestidade", "Falta de ambição", "Dependência emocional extrema", "Arrogância ou ego inflado", "Falta de respeito com os outros", "Ciúmes e controle excessivo")),
    Question(18, "2. Você acredita em fidelidade como valor central?", listOf("Sim, totalmente", "Sim, mas depende do tipo de relação", "Acredito em liberdade com respeito", "Não é algo essencial pra mim")),
    Question(19, "3. Como lida com o espaço individual dentro de um relacionamento?", listOf("Acho essencial, cada um precisa do seu", "Gosto de estar junto, mas entendo o espaço", "Prefiro ficar sempre perto", "Depende do tipo de conexão")),
    Question(20, "4. Qual dessas frases representa melhor o que você busca?", listOf("Quero um parceiro(a) que some e não sufoque", "Quero construir algo com equilíbrio e verdade", "Busco alguém que entenda minhas individualidades", "Procuro intensidade e entrega total"))
)

/**
 * Tela de perguntas "Limites & Valores", desacoplada do ViewModel.
 */
@Composable
fun LimitesScreen(
    selectedAnswers: Map<Int, List<String>>,
    onAnswerChange: (questionId: Int, newAnswers: List<String>) -> Unit,
    onNavigateToNext: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            AppTopBar(title = "LIMITES", onBackClick = { /* TODO */ })
        },
        bottomBar = {
            PrimaryButton(
                text = "Próximo",
                onClick = onNavigateToNext,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                // O botão só fica habilitado se TODAS as perguntas desta tela forem respondidas.
                enabled = limitsAndValuesQuestions.all { question -> selectedAnswers.containsKey(question.id) }
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
            items(limitsAndValuesQuestions) { question ->
                LimitesQuestionCard(
                    question = question,
                    selectedOptions = selectedAnswers[question.id] ?: emptyList(),
                    onOptionSelected = { optionText ->
                        val currentAnswers = selectedAnswers[question.id]?.toMutableList() ?: mutableListOf()
                        val isMultiSelectQuestion = question.id == 17
                        val maxSelections = 2

                        if (currentAnswers.contains(optionText)) {
                            currentAnswers.remove(optionText)
                        } else {
                            if (isMultiSelectQuestion) {
                                if (currentAnswers.size < maxSelections) {
                                    currentAnswers.add(optionText)
                                }
                            } else {
                                currentAnswers.clear()
                                currentAnswers.add(optionText)
                            }
                        }
                        // Avisa o ViewModel da mudança
                        onAnswerChange(question.id, currentAnswers)
                    }
                )
            }
        }
    }
}


// =============================================================================
// COMPONENTES DESTA TELA - Mantendo a estrutura autocontida e fiel
// =============================================================================

@Composable
fun LimitesQuestionCard(
    question: Question,
    selectedOptions: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = question.text, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (question.id == 17) {
                Text(
                    text = "selecione até 2 opções",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            question.options.forEach { optionText ->
                LimitesSelectableOptionRow(
                    text = optionText,
                    isSelected = optionText in selectedOptions,
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
fun LimitesSelectableOptionRow(
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
fun LimitesScreenPreview() {
    LioraTheme {
        // O Preview simula os dados para funcionar sem um ViewModel real.
        var dummyAnswers by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }
        LimitesScreen(
            selectedAnswers = dummyAnswers,
            onAnswerChange = { qId, newAnswers -> dummyAnswers = dummyAnswers + (qId to newAnswers) },
            onNavigateToNext = {}
        )
    }
}
