package com.example.liora.ui.onboarding // Coloque no mesmo pacote do seu OnboardingViewModel

/**
 * Representa os diferentes estados da operação de submissão do perfil.
 * Usar uma sealed interface/class é uma ótima prática para modelar estados
 * que têm um número finito e conhecido de variações.
 */
sealed interface SubmissionState {

    /** O estado inicial, antes de qualquer operação. */
    data object Idle : SubmissionState

    /** A operação de submissão está em andamento. A UI deve mostrar um loading. */
    data object Loading : SubmissionState

    /** A operação foi concluída com sucesso. */
    data object Success : SubmissionState

    /** A operação falhou. Contém uma mensagem de erro para ser exibida. */
    data class Error(val message: String) : SubmissionState
}