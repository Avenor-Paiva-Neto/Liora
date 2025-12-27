package com.example.liora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liora.ui.theme.*

/**
 * Tela para exibir os Termos de Uso e Política de Privacidade.
 * Utiliza o texto refinado e profissional fornecido.
 */
@Composable
fun TermsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // --- Barra de Topo ---
        // Usando o componente padrão que já temos.
        AppTopBar(title = "PRIVACIDADE", onBackClick = onBackClick)

        // --- Conteúdo Rolável dos Termos ---
        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Seja bem-vindo(a) ao Liora! Ao utilizar o nosso aplicativo, você concorda com os termos e condições descritos abaixo. Recomendamos a leitura atenta deste documento antes de criar uma conta ou utilizar os serviços do Liora.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                LegalSection(
                    title = "1. Aceitação dos Termos",
                    content = "Ao acessar, instalar ou utilizar o Liora, você declara ter lido, compreendido e aceitado integralmente estes Termos de Uso e a Política de Privacidade. Se não concordar com qualquer cláusula, recomendamos que não utilize o aplicativo."
                )
            }
            item {
                LegalSection(
                    title = "2. Sobre o Liora",
                    content = "O Liora é um aplicativo de relacionamento que busca promover conexões reais e seguras entre pessoas. Utilizamos um sistema de algoritmos personalizados que considera suas informações e interações para sugerir perfis compatíveis."
                )
            }
            item {
                LegalSection(
                    title = "3. Cadastro e Responsabilidades do Usuário",
                    content = "Você deve ter pelo menos 18 anos de idade. Ao se registrar, você concorda em fornecer informações verdadeiras, manter a confidencialidade de suas credenciais e ser o único responsável por qualquer atividade realizada em sua conta. Você se compromete a não usar o Liora para fins ilegais, não publicar conteúdo ofensivo e não assediar outros usuários."
                )
            }
            item {
                LegalSection(
                    title = "4. Privacidade e Proteção de Dados (LGPD)",
                    content = "Nós tratamos seus dados com seriedade e responsabilidade, conforme a Lei Geral de Proteção de Dados (LGPD). Coletamos as informações que você fornece para operar nosso algoritmo, personalizar sua experiência e garantir a segurança. Jamais vendemos seus dados pessoais. Você pode, a qualquer momento, acessar, corrigir ou solicitar a exclusão da sua conta e dos seus dados."
                )
            }
            item {
                LegalSection(
                    title = "5. Limitação de Responsabilidade",
                    content = "O Liora não garante que você encontrará um relacionamento ou que todos os matches serão compatíveis. Não nos responsabilizamos por comportamentos de terceiros. Recomendamos que evite compartilhar informações sensíveis em conversas."
                )
            }
            item {
                LegalSection(
                    title = "6. Contato e Suporte",
                    content = "Em caso de dúvidas, sugestões ou solicitações, entre em contato conosco pelo canal de suporte do app ou pelo e-mail: suporte@liora.app"
                )
            }
            item {
                Text(
                    text = "Boa sorte nas suas conexões! 💜",
                    color = AccentPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

// Componente reutilizável para cada seção do texto legal.
@Composable
fun LegalSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = content,
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp // Espaçamento entre linhas para melhor legibilidade
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TermsScreenPreview() {
    LioraTheme {
        TermsScreen(onBackClick = {})
    }
}
