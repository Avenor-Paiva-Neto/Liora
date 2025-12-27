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
val hobbiesAndInterestsQuestions = listOf(
    Question(6, "1. Qual dessas atividades te representa melhor no tempo livre?", listOf("Maratonar séries/filmes", "Ler ou escrever", "Jogar (PC, console ou mobile)", "Sair com amigos", "Criar algo (desenhar, editar, compor, etc.)", "Outra vibe (vou contar no final do perfil)")),
    Question(7, "2. Quando pensa em lazer, o que mais atrai você?", listOf("Cultura (museus, shows, eventos)", "Esporte ou academia", "Natureza e viagens", "Festas e baladas", "Ficar de boa, relaxando em casa")),
    Question(8, "3. Qual dessas paixões você mais curte conversar sobre?", listOf("Música", "Filmes e séries", "Games", "Livros e filosofia", "Tecnologia e futuro", "Memes e cultura pop")),
    Question(9, "4. Qual dessas experiências você adoraria viver com alguém?", listOf("Viajar pra um lugar inusitado", "Cozinhar algo juntos", "Ter noites de jogos ou maratona", "Fazer um projeto criativo juntos", "Ficar em silêncio confortável só curtindo")),
    Question(10, "5. Qual dessas frases mais parece com você?", listOf("Minha mente nunca para", "Eu vivo colecionando momentos", "Me perco fácil em boas histórias", "Gosto de rir até a barriga doer", "Curto viver no meu próprio ritmo")),
    Question(11, "6. Em um encontro ideal, você preferiria...", listOf("Algo simples e real, tipo um café e boa conversa", "Um rolê diferente, tipo karaokê, escape room ou algo fora do comum", "Ir pra um evento, show ou festa", "Um dia mais off, filme, coberta e risada"))
)

/**
 * Tela de perguntas "Hobbies & Interesses", desacoplada do ViewModel.
 */
// ALTERAÇÃO 1: A função agora recebe os DADOS e as AÇÕES, não o ViewModel inteiro.
@Composable
fun HobbiesScreen(
    selectedAnswers: Map<Int, List<String>>,
    onAnswerChange: (questionId: Int, newAnswers: List<String>) -> Unit,
    onNavigateToNext: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            AppTopBar(title = "HOBBIES", onBackClick = { /* TODO */ })
        },
        bottomBar = {
            PrimaryButton(
                text = "Próximo",
                onClick = onNavigateToNext,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = hobbiesAndInterestsQuestions.all { question -> selectedAnswers.containsKey(question.id) }
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
            items(hobbiesAndInterestsQuestions) { question ->
                HobbiesQuestionCard(
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
fun HobbiesQuestionCard(
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
                HobbiesSelectableOptionRow(
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
fun HobbiesSelectableOptionRow(
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
fun HobbiesScreenPreview() {
    LioraTheme {
        // ALTERAÇÃO 2: O Preview agora funciona perfeitamente, pois simula os dados.
        // Ele não constrói um ViewModel, evitando o erro.
        var dummyAnswers by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }
        HobbiesScreen(
            selectedAnswers = dummyAnswers,
            onAnswerChange = { qId, newAnswers -> dummyAnswers = dummyAnswers + (qId to newAnswers) },
            onNavigateToNext = {}
        )
    }
}
