package com.bluepatitas.mobile.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.core.designsystem.components.LoadingContent
import com.bluepatitas.mobile.core.designsystem.theme.BluePatitasTheme
import com.bluepatitas.mobile.core.navigation.RoleNavigationScaffold
import com.bluepatitas.mobile.domain.model.AppLanguage
import com.bluepatitas.mobile.feature.developer.DeveloperEntryScreen

@Composable
fun BluePatitasApp(
    viewModel: AppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSession = uiState.session
    val configuration = LocalConfiguration.current
    val activeLanguage = remember(configuration) {
        AppLanguage.fromTag(configuration.locales[0].toLanguageTag())
    }

    BluePatitasTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize()) {
                    LoadingContent()
                }

                uiState.session == null -> DeveloperEntryScreen(
                    selectedLanguage = activeLanguage,
                    onLanguageSelected = viewModel::selectLanguage,
                    onEnterAsAdministrator = { viewModel.enterDemo(com.bluepatitas.mobile.domain.model.UserRole.SHELTER_ADMIN) },
                    onEnterAsVeterinarian = { viewModel.enterDemo(com.bluepatitas.mobile.domain.model.UserRole.VETERINARIAN) }
                )

                currentSession != null -> RoleNavigationScaffold(
                    session = currentSession,
                    onSignOut = viewModel::signOut
                )
            }
        }
    }
}
