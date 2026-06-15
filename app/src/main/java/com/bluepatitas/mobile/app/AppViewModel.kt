package com.bluepatitas.mobile.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.domain.model.AppLanguage
import com.bluepatitas.mobile.domain.model.AppPreferences
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.usecase.ClearSessionUseCase
import com.bluepatitas.mobile.domain.usecase.ObserveAppPreferencesUseCase
import com.bluepatitas.mobile.domain.usecase.ObserveSessionUseCase
import com.bluepatitas.mobile.domain.usecase.SetLanguageUseCase
import com.bluepatitas.mobile.domain.usecase.StartDemoSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BluePatitasAppUiState(
    val session: AppSession? = null,
    val preferences: AppPreferences = AppPreferences(
        language = AppLanguage.ENGLISH,
        isDemoModeEnabled = true
    ),
    val isLoading: Boolean = true
)

@HiltViewModel
class AppViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    observeAppPreferences: ObserveAppPreferencesUseCase,
    private val startDemoSession: StartDemoSessionUseCase,
    private val clearSession: ClearSessionUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val appLocaleController: com.bluepatitas.mobile.core.util.AppLocaleController
) : ViewModel() {

    val uiState: StateFlow<BluePatitasAppUiState> = combine(
        observeSession(),
        observeAppPreferences()
    ) { session, preferences ->
        BluePatitasAppUiState(
            session = session,
            preferences = preferences,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BluePatitasAppUiState()
    )

    fun enterDemo(role: UserRole) {
        viewModelScope.launch {
            startDemoSession(role)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            clearSession()
        }
    }

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            setLanguage(language)
            appLocaleController.apply(language)
        }
    }
}
