package com.example.liora.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.liora.ui.components.AppTopBar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liora.ui.theme.*
import java.util.Calendar

@Composable
fun PreferencesScreen(
    // Estado da Localização
    isLocating: Boolean,
    locationName: String,
    onLocateUser: () -> Unit,

    // Estado da Idade do Usuário
    userBirthYear: Int?,
    onUserBirthYearChange: (Int) -> Unit,

    // Outros estados
    distance: Float,
    onDistanceChange: (Float) -> Unit,
    ageRange: ClosedFloatingPointRange<Float>,
    onAgeRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    lookingFor: String,
    onLookingForChange: (String) -> Unit,
    sexuality: String,
    onSexualityChange: (String) -> Unit,
    wants: String,
    onWantsChange: (String) -> Unit,
    onNavigateToNext: () -> Unit
) {
    // ===================================================================
    // A LÓGICA DE VALIDAÇÃO AGORA VIVE AQUI DENTRO
    // ===================================================================
    val isButtonEnabled = locationName != "Toque para localizar" &&
            !isLocating &&
            userBirthYear != null &&
            lookingFor.isNotBlank() &&
            sexuality.isNotBlank() &&
            wants.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        AppTopBar(title = "FILTROS", onBackClick = { /* TODO: Ação de voltar */ })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            item {
                LocationCard(
                    isLocating = isLocating,
                    locationText = locationName,
                    onLocateClick = onLocateUser
                )
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("ANO DE NASCIMENTO")
                BirthYearSelector(
                    selectedYear = userBirthYear,
                    onYearSelected = onUserBirthYearChange
                )
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("DISTANCIA")
                DistanceSlider(value = distance, onValueChange = onDistanceChange)
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("IDADE (PARCEIRO)")
                AgeSlider(value = ageRange, onValueChange = onAgeRangeChange)
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("PROCURANDO")
                ChipGroup(
                    options = listOf("Homen", "Mulher", "Outros"),
                    selectedOption = lookingFor,
                    onOptionSelected = onLookingForChange
                )
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("SEXUALIDADE")
                ChipGroup(
                    options = listOf("Hetero", "Homo", "Lesbica", "Bissexual", "Outros"),
                    selectedOption = sexuality,
                    onOptionSelected = onSexualityChange
                )
                Spacer(Modifier.height(40.dp))
            }

            item {
                SectionTitle("VOCÊ QUER ?")
                ChipGroup(
                    options = listOf("Amizade", "Nada serio", "Não sei", "Relacionamento", "O tempo dira"),
                    selectedOption = wants,
                    onOptionSelected = onWantsChange
                )
                Spacer(Modifier.height(60.dp))
            }

            // ===================================================================
            // A CHAMADA CORRIGIDA E ÚNICA PARA A SEÇÃO FINAL
            // ===================================================================
            item {
                FinalSection(
                    isButtonEnabled = isButtonEnabled,
                    onNavigateToNext = onNavigateToNext
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// =============================================================================
// COMPONENTES AUXILIARES (NENHUMA ALTERAÇÃO, EXCETO NA ASSINATURA DA FinalSection)
// =============================================================================

@Composable
fun LocationCard(isLocating: Boolean, locationText: String, onLocateClick: () -> Unit) {
    var animatedText by remember { mutableStateOf("LOCALIZANDO") }
    LaunchedEffect(isLocating) {
        if (isLocating) {
            val baseText = "LOCALIZANDO"
            var dots = 0
            while (true) {
                animatedText = baseText + ".".repeat(dots)
                dots = (dots + 1) % 4
                kotlinx.coroutines.delay(400)
            }
        }
    }
    val textToShow = if (isLocating) animatedText else locationText
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ChipBackground).clickable(onClick = onLocateClick).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, "Localização", tint = TextPrimary)
        Spacer(Modifier.width(16.dp))
        Text(textToShow, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthYearSelector(selectedYear: Int?, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 100..currentYear - 18).toList().reversed()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedYear?.toString() ?: "Selecione seu ano de nascimento",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = ChipBackground, focusedBorderColor = AccentPrimary,
                unfocusedTextColor = TextSecondary, focusedTextColor = TextPrimary
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(ChipBackground)) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString(), color = TextPrimary) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Composable
fun SectionTitle(title: String) {
    Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
}

@Composable
fun DistanceSlider(value: Float, onValueChange: (Float) -> Unit) {
    Slider(
        value = value, onValueChange = onValueChange, valueRange = 0f..100f,
        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary, inactiveTrackColor = ChipBackground)
    )
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text("0 KM", color = TextSecondary, fontSize = 12.sp)
        Text("${value.toInt()} KM", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun AgeSlider(value: ClosedFloatingPointRange<Float>, onValueChange: (ClosedFloatingPointRange<Float>) -> Unit) {
    RangeSlider(
        value = value, onValueChange = onValueChange, valueRange = 18f..70f,
        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary, inactiveTrackColor = ChipBackground)
    )
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(value.start.toInt().toString(), color = TextSecondary, fontSize = 12.sp)
        Text(value.endInclusive.toInt().toString(), color = TextSecondary, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { text ->
            val isSelected = text == selectedOption
            val backgroundColor = if (isSelected) AccentPrimary else ChipBackground
            val textColor = if (isSelected) AccentText else TextSecondary
            Box(
                modifier = Modifier.clip(RoundedCornerShape(100)).background(backgroundColor).clickable { onOptionSelected(text) }.padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FinalSection(
    isButtonEnabled: Boolean, // <-- Assinatura corrigida
    onNavigateToNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TUDO PRONTO", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Default.Check, "Pronto", tint = AccentPrimary, modifier = Modifier.background(ChipBackground, CircleShape).padding(4.dp))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNavigateToNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, disabledContainerColor = ChipBackground),
            enabled = isButtonEnabled // <-- Conectado corretamente
        ) {
            Text(text = "Próximo", color = AccentText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreferencesScreenPreview() {
    LioraTheme {
        var isLocating by remember { mutableStateOf(true) }
        var locationName by remember { mutableStateOf("Araxá, MG") }
        var userBirthYear by remember { mutableStateOf<Int?>(1995) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(5000)
            isLocating = false
        }
        PreferencesScreen(
            isLocating = isLocating, locationName = locationName,
            onLocateUser = { isLocating = true }, userBirthYear = userBirthYear,
            onUserBirthYearChange = { userBirthYear = it }, distance = 30f, onDistanceChange = {},
            ageRange = 22f..35f, onAgeRangeChange = {}, lookingFor = "Mulher", onLookingForChange = {},
            sexuality = "Hetero", onSexualityChange = {}, wants = "Relacionamento", onWantsChange = {},
            onNavigateToNext = {}
        )
    }
}