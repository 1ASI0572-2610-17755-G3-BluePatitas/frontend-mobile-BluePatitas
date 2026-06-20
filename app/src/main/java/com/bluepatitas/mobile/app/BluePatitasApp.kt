package com.bluepatitas.mobile.app

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.bluepatitas.mobile.core.designsystem.theme.BluePatitasTheme
import com.bluepatitas.mobile.core.navigation.AppRoute
import com.bluepatitas.mobile.core.navigation.RoleNavigationScaffold
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.feature.auth.ForgotPasswordRoute
import com.bluepatitas.mobile.feature.auth.InvitationRoute
import com.bluepatitas.mobile.feature.auth.LoginRoute
import com.bluepatitas.mobile.feature.auth.RegisterRoute
import com.bluepatitas.mobile.feature.auth.SplashScreen
import com.bluepatitas.mobile.feature.auth.WelcomeScreen
import com.bluepatitas.mobile.feature.auth.activeLanguageFromResources
import com.bluepatitas.mobile.feature.developer.DeveloperEntryScreen
import com.bluepatitas.mobile.feature.onboarding.ShelterBasicInfoRoute
import com.bluepatitas.mobile.feature.onboarding.ShelterConfirmationRoute
import com.bluepatitas.mobile.feature.onboarding.ShelterLocationRoute
import com.bluepatitas.mobile.feature.onboarding.ShelterOnboardingViewModel

@Composable
fun BluePatitasApp(
    viewModel: AppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activeLanguage = activeLanguageFromResources()
    val context = LocalContext.current
    val showDeveloperAccess = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun navigateAndClear(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun adminSessionGoesToMain(): Boolean {
        val session = uiState.session ?: return false
        val isBackendSession = !session.token.isNullOrBlank()
        return session.onboardingCompleted || (!isBackendSession && uiState.shelter != null)
    }

    LaunchedEffect(uiState.isLoading, uiState.session, currentRoute) {
        if (!uiState.isLoading && uiState.session == null &&
            (currentRoute == AppRoute.AdminMain.route || currentRoute == AppRoute.VeterinarianMain.route)
        ) {
            navigateAndClear(AppRoute.Welcome.route)
        }
    }

    BluePatitasTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.Splash.route
            ) {
                composable(AppRoute.Splash.route) {
                    SplashScreen(
                        navigationKey = uiState,
                        onFinished = {
                            if (uiState.isLoading) return@SplashScreen
                            val session = uiState.session
                            val nextRoute = when (session?.role) {
                                UserRole.SHELTER_ADMIN -> {
                                    if (adminSessionGoesToMain()) {
                                        AppRoute.AdminMain.route
                                    } else {
                                        AppRoute.ShelterGraph.route
                                    }
                                }

                                UserRole.VETERINARIAN -> AppRoute.VeterinarianMain.route
                                null -> AppRoute.Welcome.route
                            }
                            navigateAndClear(nextRoute)
                        }
                    )
                }
                composable(AppRoute.Welcome.route) {
                    WelcomeScreen(
                        onGetStarted = { navController.navigate(AppRoute.Register.route) },
                        onLogin = { navController.navigate(AppRoute.Login.route) },
                        onInvitation = { navController.navigate(AppRoute.Invitation.route) },
                        onDeveloperAccess = { navController.navigate(AppRoute.DeveloperEntry.route) },
                        selectedLanguage = activeLanguage,
                        onLanguageSelected = viewModel::selectLanguage,
                        showDeveloperAccess = showDeveloperAccess
                    )
                }
                composable(AppRoute.DeveloperEntry.route) {
                    DeveloperEntryScreen(
                        selectedLanguage = activeLanguage,
                        onLanguageSelected = viewModel::selectLanguage,
                        onEnterAsAdministrator = {
                            viewModel.enterDemo(UserRole.SHELTER_ADMIN)
                            navigateAndClear(AppRoute.AdminMain.route)
                        },
                        onEnterAsVeterinarian = {
                            viewModel.enterDemo(UserRole.VETERINARIAN)
                            navigateAndClear(AppRoute.VeterinarianMain.route)
                        }
                    )
                }
                composable(AppRoute.Login.route) {
                    LoginRoute(
                        onForgotPassword = { navController.navigate(AppRoute.ForgotPassword.route) },
                        onRegister = { navController.navigate(AppRoute.Register.route) },
                        onAdminOnboarding = { navigateAndClear(AppRoute.ShelterGraph.route) },
                        onAdminMain = { navigateAndClear(AppRoute.AdminMain.route) },
                        onVeterinarianMain = { navigateAndClear(AppRoute.VeterinarianMain.route) },
                        selectedLanguage = activeLanguage,
                        onLanguageSelected = viewModel::selectLanguage
                    )
                }
                composable(AppRoute.Register.route) {
                    RegisterRoute(
                        onCompleted = { navigateAndClear(AppRoute.Login.route) },
                        onLogin = { navController.navigate(AppRoute.Login.route) }
                    )
                }
                composable(AppRoute.ForgotPassword.route) {
                    ForgotPasswordRoute(onBackToLogin = { navController.popBackStack() })
                }
                composable(AppRoute.Invitation.route) {
                    InvitationRoute(onCompleted = { navigateAndClear(AppRoute.VeterinarianMain.route) })
                }
                navigation(
                    startDestination = AppRoute.ShelterBasicInfo.route,
                    route = AppRoute.ShelterGraph.route
                ) {
                    composable(AppRoute.ShelterBasicInfo.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(AppRoute.ShelterGraph.route)
                        }
                        val shelterViewModel: ShelterOnboardingViewModel = hiltViewModel(parentEntry)
                        ShelterBasicInfoRoute(
                            onContinue = { navController.navigate(AppRoute.ShelterLocation.route) },
                            onCancel = { navigateAndClear(AppRoute.Welcome.route) },
                            viewModel = shelterViewModel
                        )
                    }
                    composable(AppRoute.ShelterLocation.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(AppRoute.ShelterGraph.route)
                        }
                        val shelterViewModel: ShelterOnboardingViewModel = hiltViewModel(parentEntry)
                        ShelterLocationRoute(
                            onBack = { navController.popBackStack() },
                            onCreated = { navController.navigate(AppRoute.ShelterConfirmation.route) },
                            viewModel = shelterViewModel
                        )
                    }
                    composable(AppRoute.ShelterConfirmation.route) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(AppRoute.ShelterGraph.route)
                        }
                        val shelterViewModel: ShelterOnboardingViewModel = hiltViewModel(parentEntry)
                        ShelterConfirmationRoute(
                            onGoToShelter = { navigateAndClear(AppRoute.AdminMain.route) },
                            viewModel = shelterViewModel
                        )
                    }
                }
                composable(AppRoute.AdminMain.route) {
                    uiState.session?.let { session ->
                        RoleNavigationScaffold(
                            session = session,
                            onSignOut = {
                                viewModel.signOut()
                                navigateAndClear(AppRoute.Welcome.route)
                            },
                            modifier = Modifier
                        )
                    }
                }
                composable(AppRoute.VeterinarianMain.route) {
                    uiState.session?.let { session ->
                        RoleNavigationScaffold(
                            session = session,
                            onSignOut = {
                                viewModel.signOut()
                                navigateAndClear(AppRoute.Welcome.route)
                            },
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}
