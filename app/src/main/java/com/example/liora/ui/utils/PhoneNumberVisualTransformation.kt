package com.example.liora.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Aplica uma máscara de telefone (XX) XXXXX-XXXX ao campo de texto.
 */
class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 11) text.text.substring(0..10) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += " "
            if (i == 6) out += "-"
        }

        // Adiciona parênteses ao redor do DDD
        if (out.length > 2) {
            out = "(${out.substring(0, 2)})${out.substring(2)}"
        }

        val phoneNumberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 6) return offset + 3 // +2 para parenteses, +1 para espaço
                if (offset <= 11) return offset + 4 // +3 anteriores, +1 para traço
                return 15
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 9) return offset - 3
                if (offset <= 15) return offset - 4
                return 11
            }
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetTranslator)
    }
}