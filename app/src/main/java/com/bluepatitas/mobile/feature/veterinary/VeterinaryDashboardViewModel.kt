package com.bluepatitas.mobile.feature.veterinary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.VeterinaryDashboard
import com.bluepatitas.mobile.domain.usecase.GetVeterinaryDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VeterinaryDashboardUiState(
    val isLoading: Boolean = true,
    val dashboard: VeterinaryDashboard? = null,
    val hasError: Boolean = false
)

@HiltViewModel
class VeterinaryDashboardViewModel @Inject constructor(
    private val getVeterinaryDashboardUseCase: GetVeterinaryDashboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeterinaryDashboardUiState())
    val uiState: StateFlow<VeterinaryDashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            when (val result = getVeterinaryDashboardUseCase()) {
                is BluePatitasResult.Success -> _uiState.update {
                    it.copy(isLoading = false, dashboard = result.value, hasError = false)
                }

                is BluePatitasResult.Error -> _uiState.update {
                    it.copy(isLoading = false, hasError = true)
                }
            }
        }
    }
}
