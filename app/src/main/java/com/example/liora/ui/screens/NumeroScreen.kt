package com.example.liora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liora.ui.theme.*
import com.example.liora.ui.utils.PhoneNumberVisualTransformation

/**
 * Tela para o usuário inserir o número de telefone, agora com validação e formatação.
 */
@Composable
fun NumeroScreen(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    countryCode: String,        // <-- NOVO: Recebe o código do país
    isButtonEnabled: Boolean,   // <-- NOVO: Recebe o estado de habilitação do botão
    onNavigateToNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary)
    ) {
        AppTopBar(title = "SEGURANÇA", onBackClick = { /* TODO */ })

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SEU NÚMERO", color = TextSecondary, fontSize = 14.sp) // Título ajustado
            Spacer(modifier = Modifier.height(8.dp))

            // --- Campo de Texto com Código do País ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Exibe o código do país
                Text(
                    text = countryCode,
                    style = TextStyle(color = AccentPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Campo de texto agora usa a máscara
                TextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    placeholder = { Text(text = "(00) 00000-0000", style = TextStyle(color = TextSecondary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)) },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    // A MÁGICA ACONTECE AQUI:
                    visualTransformation = PhoneNumberVisualTransformation()
                )
            }
            Divider(color = TextSecondary, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Digite seu numero com o ddd para sua segurança", color = TextSecondary, fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // O botão agora usa o novo estado para se habilitar
            Button(
                onClick = onNavigateToNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, disabledContainerColor = ChipBackground),
                enabled = isButtonEnabled
            ) {
                Text(text = "Próximo", color = AccentText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// Preview atualizado para refletir a nova assinatura da função
@Preview(showBackground = true)
@Composable
fun NumeroScreenPreview() {
    LioraTheme {
        var dummyNumber by remember { mutableStateOf("34999999999") }
        NumeroScreen(
            phoneNumber = dummyNumber,
            onPhoneNumberChange = { dummyNumber = it },
            countryCode = "+55",
            isButtonEnabled = true,
            onNavigateToNext = {}
        )
    }
}