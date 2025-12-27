package com.example.liora.ui

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.liora.di.AppViewModelFactory
import com.example.liora.ui.components.LioraBottomNavBar
import com.example.liora.ui.onboarding.OnboardingViewModel
import com.example.liora.ui.onboarding.SubmissionState
import com.example.liora.ui.screens.*
import com.example.liora.ui.chat.ChatScreen
import com.example.liora.ui.conversations.ConversationsScreen

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val onboardingViewModel: OnboardingViewModel = viewModel(factory = AppViewModelFactory(application))

    val appEntryViewModel: AppEntryViewModel = viewModel(factory = AppViewModelFactory(application))
    val appEntryState by appEntryViewModel.uiState.collectAsState()

    NavHost(
        navController = rootNavController, startDestination = AppRoutes.SPLASH,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350)) }
    ) {
        composable(AppRoutes.SPLASH) {
            val context = LocalContext.current
            SplashScreen(
                appEntryState = appEntryState,
                onAnimationComplete = { finalAppEntryState ->
                    when (finalAppEntryState) {
                        is AppEntryUiState.AuthenticatedAndProfileExists -> {
                            rootNavController.navigate(AppRoutes.MAIN_APP_HOST) {
                                popUpTo(AppRoutes.SPLASH) { inclusive = true }
                            }
                            appEntryViewModel.resetState()
                        }
                        is AppEntryUiState.Unauthenticated -> {
                            rootNavController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.SPLASH) { inclusive = true }
                            }
                            appEntryViewModel.resetState()
                        }
                        is AppEntryUiState.AuthenticatedButNoProfile -> {
                            rootNavController.navigate(AppRoutes.NUMERO) {
                                popUpTo(AppRoutes.SPLASH) { inclusive = true }
                            }
                            appEntryViewModel.resetState()
                        }
                        is AppEntryUiState.Error -> {
                            Toast.makeText(context, "Erro inicial: ${finalAppEntryState.message}", Toast.LENGTH_LONG).show()
                            rootNavController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.SPLASH) { inclusive = true }
                            }
                            appEntryViewModel.resetState()
                        }
                        AppEntryUiState.Loading -> {}
                    }
                }
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToDiscovery = {
                    rootNavController.navigate(AppRoutes.MAIN_APP_HOST) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToCadastro = {
                    rootNavController.navigate(AppRoutes.NUMERO)
                }
            )
        }

        composable(AppRoutes.NUMERO) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                onboardingViewModel.detectUserCountry(context)
            }
            val phoneNumber by onboardingViewModel.phoneNumber.collectAsState()
            val countryCode by onboardingViewModel.countryDialCode.collectAsState()
            val isPhoneNumberValid by onboardingViewModel.isPhoneNumberValid.collectAsState()
            NumeroScreen(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = onboardingViewModel::onPhoneNumberChange,
                countryCode = countryCode,
                isButtonEnabled = isPhoneNumberValid,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.NOME) }
            )
        }

        composable(AppRoutes.NOME) {
            val name by onboardingViewModel.name.collectAsState()
            NameScreen(
                name = name,
                onNameChange = onboardingViewModel::onNameChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.PREFERENCIAS) }
            )
        }

        composable(AppRoutes.PREFERENCIAS) {
            val context = LocalContext.current
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                        onboardingViewModel.onLocateUser(context)
                    } else {
                        Toast.makeText(context, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            val isLocating by onboardingViewModel.isLocating.collectAsState()
            val locationName by onboardingViewModel.locationName.collectAsState()
            val userBirthYear by onboardingViewModel.birthYear.collectAsState()
            val distance by onboardingViewModel.distance.collectAsState()
            val ageRange by onboardingViewModel.ageRange.collectAsState()
            val lookingFor by onboardingViewModel.lookingFor.collectAsState()
            val sexuality by onboardingViewModel.sexuality.collectAsState()
            val wants by onboardingViewModel.wants.collectAsState()
            PreferencesScreen(
                isLocating = isLocating,
                locationName = locationName,
                onLocateUser = {
                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                userBirthYear = userBirthYear,
                onUserBirthYearChange = onboardingViewModel::onUserBirthYearChange,
                distance = distance,
                onDistanceChange = onboardingViewModel::onDistanceChange,
                ageRange = ageRange,
                onAgeRangeChange = onboardingViewModel::onAgeRangeChange,
                lookingFor = lookingFor,
                onLookingForChange = onboardingViewModel::onLookingForChange,
                sexuality = sexuality,
                onSexualityChange = onboardingViewModel::onSexualityChange,
                wants = wants,
                onWantsChange = onboardingViewModel::onWantsChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.SOBRE) }
            )
        }

        composable(AppRoutes.SOBRE) {
            val answers by onboardingViewModel.answers.collectAsState()
            SobreScreen(
                selectedAnswers = answers,
                onAnswerChange = onboardingViewModel::onAnswerChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.HOBBIES) }
            )
        }

        composable(AppRoutes.HOBBIES) {
            val answers by onboardingViewModel.answers.collectAsState()
            HobbiesScreen(
                selectedAnswers = answers,
                onAnswerChange = onboardingViewModel::onAnswerChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.PERSONALIDADE) }
            )
        }

        composable(AppRoutes.PERSONALIDADE) {
            val answers by onboardingViewModel.answers.collectAsState()
            PersonalidadeScreen(
                selectedAnswers = answers,
                onAnswerChange = onboardingViewModel::onAnswerChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.LIMITES) }
            )
        }

        composable(AppRoutes.LIMITES) {
            val answers by onboardingViewModel.answers.collectAsState()
            LimitesScreen(
                selectedAnswers = answers,
                onAnswerChange = onboardingViewModel::onAnswerChange,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.IMAGEM) }
            )
        }

        composable(AppRoutes.IMAGEM) {
            val imageUris by onboardingViewModel.imageUris.collectAsState()
            ImageScreen(
                imageUris = imageUris,
                onImageSelected = onboardingViewModel::onImageSelected,
                onNavigateToNext = { rootNavController.navigate(AppRoutes.BIO) },
                onBackClick = { rootNavController.popBackStack() }
            )
        }

        composable(AppRoutes.BIO) {
            val context = LocalContext.current
            val submissionState by onboardingViewModel.submissionState.collectAsState()
            val bioText by onboardingViewModel.bio.collectAsState()
            val termsChecked by onboardingViewModel.termsChecked.collectAsState()

            LaunchedEffect(submissionState) {
                when (val state = submissionState) {
                    is SubmissionState.Success -> {
                        Toast.makeText(context, "Perfil criado com sucesso!", Toast.LENGTH_SHORT).show()
                        rootNavController.navigate(AppRoutes.MAIN_APP_HOST) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    }
                    is SubmissionState.Error -> {
                        Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                        onboardingViewModel.resetSubmissionState()
                    }
                    else -> {}
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                BioScreen(
                    bioText = bioText,
                    onBioChange = onboardingViewModel::onBioChange,
                    termsChecked = termsChecked,
                    onTermsCheckedChange = onboardingViewModel::onTermsCheckedChange,
                    onFinalize = onboardingViewModel::finalizeOnboarding,
                    onNavigateToTerms = { rootNavController.navigate(AppRoutes.TERMOS) },
                    onBackClick = { rootNavController.popBackStack() }
                )
                if (submissionState is SubmissionState.Loading) {
                    LoadingOverlay()
                }
            }
        }

        composable(AppRoutes.TERMOS) { /* Seu código original aqui, sem alterações */ }

        composable(
            route = AppRoutes.INDIVIDUAL_CHAT_ROUTE_WITH_ARGS,
            arguments = listOf(navArgument("matchedUserId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchedUserId = backStackEntry.arguments?.getString("matchedUserId")
            if (matchedUserId != null) {
                ChatScreen(navController = rootNavController, matchedUserId = matchedUserId)
            } else {
                Toast.makeText(LocalContext.current, "Erro: ID do usuário do chat não encontrado.", Toast.LENGTH_SHORT).show()
                rootNavController.popBackStack()
            }
        }

        composable(AppRoutes.MAIN_APP_HOST) {
            MainAppScreen(rootNavController = rootNavController)
        }
    }
}

@Composable
fun MainAppScreen(rootNavController: NavController) {
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            LioraBottomNavBar(
                navController = mainNavController,
                currentRoute = currentRoute
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = AppRoutes.DISCOVERY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.DISCOVERY) { DiscoveryScreen(navController = mainNavController) }

            composable(AppRoutes.LIKES) {
                LikesScreen(
                    onNavigateBack = {
                        mainNavController.popBackStack()
                    },
                    onNavigateToProfile = { userId ->
                        // Navega para a tela de perfil usando o controlador de navegação principal.
                        rootNavController.navigate("${AppRoutes.PERFIL}/$userId")
                    },
                    // CORREÇÃO AQUI: Parâmetro adicionado
                    onNavigateToChat = { userId ->
                        // Navega para a tela de chat usando o controlador de navegação raiz.
                        rootNavController.navigate("${AppRoutes.INDIVIDUAL_CHAT}/$userId")
                    }
                )
            }

            composable(AppRoutes.CHAT_LIST) {
                ConversationsScreen(navController = rootNavController)
            }

            composable("${AppRoutes.PERFIL}/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                PerfilScreen(/* Se precisar, passe o userId para a tela aqui */)
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Finalizando seu perfil...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}