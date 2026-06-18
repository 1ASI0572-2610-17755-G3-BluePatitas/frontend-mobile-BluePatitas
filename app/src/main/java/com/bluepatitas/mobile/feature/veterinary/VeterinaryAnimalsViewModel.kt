package com.bluepatitas.mobile.feature.veterinary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.VeterinaryAnimal
import com.bluepatitas.mobile.domain.usecase.GetVeterinaryAnimalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VeterinaryAnimalsUiState(
    val isLoading: Boolean = true,
    val animals: List<VeterinaryAnimal> = emptyList(),
    val hasError: Boolean = false
)

@HiltViewModel
class VeterinaryAnimalsViewModel @Inject constructor(
    private val getVeterinaryAnimalsUseCase: GetVeterinaryAnimalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeterinaryAnimalsUiState())
    val uiState: StateFlow<VeterinaryAnimalsUiState> = _uiState

    init {
        loadAnimals()
    }

    fun loadAnimals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            when (val result = getVeterinaryAnimalsUseCase()) {
                is BluePatitasResult.Success -> _uiState.update {
                    it.copy(isLoading = false, animals = result.value, hasError = false)
                }

                is BluePatitasResult.Error -> _uiState.update {
                    it.copy(isLoading = false, hasError = true)
                }
            }
        }
    }
}
