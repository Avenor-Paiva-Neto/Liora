package com.example.liora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liora.ui.theme.*

@Composable
fun NameScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onNavigateToNext: () -> Unit
) {
    // Estado para controlar a visibilidade do nosso diálogo de confirmação
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // ===================================================================
    // 1. O DIÁLOGO DE CONFIRMAÇÃO (NOSSO CARD FLUTUANTE)
    // ===================================================================
    if (showConfirmationDialog) {
        ConfirmNameDialog(
            name = name,
            onDismiss = { showConfirmationDialog = false }, // Fecha o diálogo
            onConfirm = {
                showConfirmationDialog = false
                onNavigateToNext() // Executa a navegação real
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(horizontal = 24.dp)
    ) {
        AppTopBar(title = "ESSENCIAL", onBackClick = { /* TODO */ })

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "EDITAR", color = TextSecondary, fontSize = 14.sp)
            Text(text = "SEU NOME", color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = name,
                onValueChange = { newName ->
                    // ===================================================================
                    // 2. A VALIDAÇÃO DO NOME (ANTI-ALEATORIEDADE)
                    // Esta expressão regular permite letras (incluindo acentos) e espaços.
                    // ===================================================================
                    if (newName.matches(Regex("^[a-zA-ZÀ-ú\\s]*$")) && newName.length <= 20) {
                        onNameChange(newName)
                    }
                },
                textStyle = TextStyle(color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(AccentPrimary),
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = TextSecondary, thickness = 1.dp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.Warning, "Aviso", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Somente o primeiro nome. Não poderá ser alterado.", color = TextSecondary, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    // ===================================================================
                    // 3. O NOVO FLUXO DO BOTÃO
                    // Em vez de navegar, ele agora abre o diálogo de confirmação.
                    // ===================================================================
                    showConfirmationDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, disabledContainerColor = ChipBackground),
                // Botão habilitado apenas se o nome tiver pelo menos 2 caracteres
                enabled = name.trim().length >= 2
            ) {
                Text(text = "Próximo", color = AccentText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/**
 * Nosso Composable para o diálogo de confirmação customizado.
 */
@Composable
fun ConfirmNameDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("CONFIRMAR", color = AccentPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        title = {
            Text("Confirmar seu nome", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            // Usamos um buildAnnotatedString para destacar o nome
            val annotatedString = buildAnnotatedString {
                append("Tem certeza que seu nome é ")
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AccentPrimary))
                append(name.trim())
                pop()
                append("? Lembre-se, ele não poderá ser alterado no futuro.")
            }
            Text(annotatedString, color = TextSecondary)
        },
        containerColor = BackgroundSecondary,
        shape = RoundedCornerShape(16.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun NameScreenPreview() {
    LioraTheme {
        var dummyName by remember { mutableStateOf("Liora") }
        NameScreen(
            name = dummyName,
            onNameChange = { dummyName = it },
            onNavigateToNext = {}
        )
    }
}