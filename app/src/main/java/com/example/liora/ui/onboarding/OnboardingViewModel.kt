package com.example.liora.ui.onboarding

import android.content.Context
import android.net.Uri
import android.telephony.TelephonyManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liora.domain.model.MatchPreferences
import com.example.liora.domain.repository.LocationRepository
import com.example.liora.domain.usecase.CreateUserProfileUseCase
import com.example.liora.domain.usecase.MapAnswersToPsycheProfileUseCase
import com.example.liora.domain.utils.PhoneNumberValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val validator: PhoneNumberValidator,
    private val locationRepository: LocationRepository,
    private val mapAnswersUseCase: MapAnswersToPsycheProfileUseCase
) : ViewModel() {

    // ===================================================================
    // PARTE 1: ESTADO DE TODOS OS DADOS COLETADOS
    // ===================================================================
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _answers = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val answers = _answers.asStateFlow()

    private val _birthYear = MutableStateFlow<Int?>(null)
    val birthYear = _birthYear.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating = _isLocating.asStateFlow()

    private val _locationName = MutableStateFlow("Toque para localizar")
    val locationName = _locationName.asStateFlow()

    private val _distance = MutableStateFlow(30f)
    val distance = _distance.asStateFlow()

    private val _ageRange = MutableStateFlow(18f..40f)
    val ageRange = _ageRange.asStateFlow()

    private val _lookingFor = MutableStateFlow("Mulher")
    val lookingFor = _lookingFor.asStateFlow()

    private val _sexuality = MutableStateFlow("Hetero")
    val sexuality = _sexuality.asStateFlow()

    private val _wants = MutableStateFlow("Relacionamento")
    val wants = _wants.asStateFlow()

    private val _imageUris = MutableStateFlow<List<Uri?>>(List(6) { null })
    val imageUris = _imageUris.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio = _bio.asStateFlow()

    private val _termsChecked = MutableStateFlow(false)
    val termsChecked = _termsChecked.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _countryRegionCode = MutableStateFlow("")
    private val _countryDialCode = MutableStateFlow("")
    val countryDialCode = _countryDialCode.asStateFlow()

    private val _isPhoneNumberValid = MutableStateFlow(false)
    val isPhoneNumberValid = _isPhoneNumberValid.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    // ===================================================================
    // PARTE 2: FUNÇÕES PÚBLICAS PARA A UI
    // ===================================================================
    fun onNameChange(newName: String) { _name.update { newName } }
    fun onUserBirthYearChange(year: Int) { _birthYear.update { year } }
    fun onAnswerChange(questionId: Int, newAnswers: List<String>) { _answers.update { it + (questionId to newAnswers) } }
    fun onDistanceChange(newDistance: Float) { _distance.update { newDistance } }
    fun onAgeRangeChange(newRange: ClosedFloatingPointRange<Float>) { _ageRange.update { newRange } }
    fun onLookingForChange(selection: String) { _lookingFor.update { selection } }
    fun onSexualityChange(selection: String) { _sexuality.update { selection } }
    fun onWantsChange(selection: String) { _wants.update { selection } }
    fun onImageSelected(uri: Uri?) {
        uri?.let {
            _imageUris.update { currentUris ->
                val newUris = currentUris.toMutableList()
                val emptyIndex = newUris.indexOfFirst { it == null }
                if (emptyIndex != -1) { newUris[emptyIndex] = uri }
                newUris
            }
        }
    }
    fun onBioChange(newBio: String) { if (newBio.length <= 300) { _bio.update { newBio } } }
    fun onTermsCheckedChange(isChecked: Boolean) { _termsChecked.update { isChecked } }

    fun onPhoneNumberChange(newNumber: String) {
        val digitsOnly = newNumber.filter { it.isDigit() }
        _phoneNumber.update { digitsOnly }
        val isValid = validator.isValid(digitsOnly, _countryRegionCode.value)
        _isPhoneNumberValid.update { isValid }
    }

    fun detectUserCountry(context: Context) {
        if (_countryRegionCode.value.isNotEmpty()) return
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val regionCode = telephonyManager.simCountryIso?.uppercase() ?: "BR"
            _countryRegionCode.value = regionCode
            _countryDialCode.value = "+${validator.getCountryCodeForRegion(regionCode)}"
        } catch (e: Exception) {
            _countryRegionCode.value = "BR"
            _countryDialCode.value = "+55"
        }
    }

    fun onLocateUser(context: Context) {
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val result = locationRepository.getCurrentCityState()
                result.onSuccess { locationString ->
                    _locationName.value = locationString
                }.onFailure {
                    _locationName.value = "Erro ao localizar"
                }
            } finally {
                _isLocating.value = false
            }
        }
    }

    // ===================================================================
    // PARTE 3: FINALIZAÇÃO COM MAPEAMENTO PSICOLÓGICO
    // ===================================================================
    fun finalizeOnboarding() {
        viewModelScope.launch {
            _submissionState.update { SubmissionState.Loading }



            val psycheProfile = mapAnswersUseCase(answers.value)
            val preferences = MatchPreferences(
                maxDistance = distance.value.toInt(),
                minAge = ageRange.value.start.toInt(),
                maxAge = ageRange.value.endInclusive.toInt(),
                lookingFor = lookingFor.value,
                wants = wants.value
            )

            val result = createUserProfileUseCase(
                name = name.value,
                birthYear = birthYear.value ?: 0,
                locationName = locationName.value,
                sexuality = sexuality.value,
                phoneNumber = phoneNumber.value,
                bio = bio.value,
                imageUris = imageUris.value.filterNotNull(),
                preferences = preferences,
                psycheProfile = psycheProfile
            )

            result.onSuccess {
                _submissionState.update { SubmissionState.Success }
            }.onFailure { exception ->
                _submissionState.update { SubmissionState.Error(exception.message ?: "Ocorreu um erro desconhecido.") }
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.update { SubmissionState.Idle }
    }
}
