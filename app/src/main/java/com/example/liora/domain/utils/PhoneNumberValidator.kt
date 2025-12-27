package com.example.liora.domain.utils

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber

/**
 * Uma classe de utilidade para validar e formatar números de telefone
 * usando a biblioteca libphonenumber do Google.
 *
 * Esta classe não tem nenhuma dependência do Android, tornando-a
 * fácil de testar e reutilizar.
 */
class PhoneNumberValidator {

    // Obtemos uma instância da biblioteca. É um objeto pesado,
    // então o criamos uma única vez.
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    /**
     * Valida um número de telefone para uma região (país) específica.
     *
     * @param phoneNumber O número de telefone a ser validado (ex: "34999999999").
     * @param regionCode O código da região de duas letras (ISO 3166-1), como "BR" para Brasil.
     * @return `true` se o número for válido para a região, `false` caso contrário.
     */
    fun isValid(phoneNumber: String, regionCode: String): Boolean {
        return try {
            val numberProto = phoneUtil.parse(phoneNumber, regionCode)
            phoneUtil.isValidNumber(numberProto)
        } catch (e: NumberParseException) {
            // Se o número for mal formatado (ex: contém letras), a biblioteca lança uma exceção.
            false
        }
    }

    /**
     * Formata um número de telefone no padrão nacional do país.
     *
     * @param phoneNumber O número a ser formatado.
     * @param regionCode O código da região (ex: "BR").
     * @return O número formatado (ex: "(34) 99999-9999") ou o número original se inválido.
     */
    fun format(phoneNumber: String, regionCode: String): String {
        return try {
            val numberProto = phoneUtil.parse(phoneNumber, regionCode)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        } catch (e: NumberParseException) {
            phoneNumber // Retorna o número original se não puder formatar
        }
    }

    /**
     * Obtém o código de discagem de um país.
     *
     * @param regionCode O código da região (ex: "BR").
     * @return O código de discagem como uma String (ex: "55").
     */
    fun getCountryCodeForRegion(regionCode: String): String {
        return phoneUtil.getCountryCodeForRegion(regionCode).toString()
    }
}