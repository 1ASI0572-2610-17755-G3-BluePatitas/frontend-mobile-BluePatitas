package com.bluepatitas.mobile.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasFormScaffold
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasInfoCard
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPrimaryButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasStepIndicator
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasTextField
import com.bluepatitas.mobile.core.designsystem.icons.PawLogoIcon
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.domain.model.ShelterProfile

@Composable
fun ShelterBasicInfoRoute(
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ShelterOnboardingViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ShelterBasicInfoScreen(
        state = state,
        onFieldChange = viewModel::updateField,
        onContinue = {
            if (viewModel.validateBasicInfo()) onContinue()
        },
        onCancel = onCancel
    )
}

@Composable
fun ShelterLocationRoute(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: ShelterOnboardingViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.createdShelter) {
        if (state.createdShelter != null) onCreated()
    }
    ShelterLocationScreen(
        state = state,
        onFieldChange = viewModel::updateField,
        onBack = onBack,
        onCreateShelter = viewModel::createShelter
    )
}

@Composable
fun ShelterConfirmationRoute(
    onGoToShelter: () -> Unit,
    viewModel: ShelterOnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ShelterConfirmationScreen(
        shelter = state.createdShelter,
        onGoToShelter = onGoToShelter
    )
}

@Composable
fun ShelterBasicInfoScreen(
    state: ShelterOnboardingUiState,
    onFieldChange: (String, String) -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.shelter_step_basic),
        subtitle = stringResource(R.string.register_subtitle),
        topContent = { ShelterSteps(currentStep = 0) }
    ) {
        BluePatitasTextField(state.draft.name, { onFieldChange("name", it) }, stringResource(R.string.shelter_name), error = state.errors["name"]?.asString())
        BluePatitasTextField(state.draft.taxId, { onFieldChange("taxId", it) }, stringResource(R.string.tax_id), error = state.errors["taxId"]?.asString(), keyboardType = KeyboardType.Number)
        BluePatitasTextField(state.draft.institutionalEmail, { onFieldChange("institutionalEmail", it) }, stringResource(R.string.institutional_email), error = state.errors["institutionalEmail"]?.asString(), keyboardType = KeyboardType.Email)
        BluePatitasTextField(state.draft.contactPhone, { onFieldChange("contactPhone", it) }, stringResource(R.string.contact_phone), error = state.errors["contactPhone"]?.asString(), keyboardType = KeyboardType.Phone)
        BluePatitasPrimaryButton(text = stringResource(R.string.continue_action), onClick = onContinue)
        BluePatitasOutlinedButton(text = stringResource(R.string.cancel), onClick = onCancel)
    }
}

@Composable
fun ShelterLocationScreen(
    state: ShelterOnboardingUiState,
    onFieldChange: (String, String) -> Unit,
    onBack: () -> Unit,
    onCreateShelter: () -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.shelter_step_location),
        subtitle = stringResource(R.string.splash_subtitle),
        topContent = { ShelterSteps(currentStep = 1) }
    ) {
        BluePatitasTextField(state.draft.address, { onFieldChange("address", it) }, stringResource(R.string.main_address), error = state.errors["address"]?.asString())
        BluePatitasTextField(state.draft.reference, { onFieldChange("reference", it) }, stringResource(R.string.reference))
        BluePatitasTextField(state.draft.district, { onFieldChange("district", it) }, stringResource(R.string.district_or_area), error = state.errors["district"]?.asString())
        BluePatitasTextField(state.draft.city, { onFieldChange("city", it) }, stringResource(R.string.city), error = state.errors["city"]?.asString())
        BluePatitasPrimaryButton(text = stringResource(R.string.create_shelter), onClick = onCreateShelter)
        BluePatitasOutlinedButton(text = stringResource(R.string.back), onClick = onBack)
    }
}

@Composable
fun ShelterConfirmationScreen(
    shelter: ShelterProfile?,
    onGoToShelter: () -> Unit
) {
    val successDescription = stringResource(R.string.success_icon_description)
    BluePatitasFormScaffold(
        title = stringResource(R.string.shelter_step_confirmation),
        subtitle = stringResource(R.string.shelter_created_title),
        topContent = { ShelterSteps(currentStep = 2) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PawLogoIcon(
                color = GreenSuccess,
                modifier = Modifier
                    .size(88.dp)
                    .semantics {
                        contentDescription = successDescription
                    }
            )
            Text(
                text = stringResource(R.string.shelter_created_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
        shelter?.let {
            BluePatitasInfoCard(title = it.name, supportingText = stringResource(R.string.shelter_summary_address, it.address, it.district, it.city))
        }
        BluePatitasPrimaryButton(text = stringResource(R.string.go_to_shelter), onClick = onGoToShelter)
    }
}

@Composable
private fun ShelterSteps(currentStep: Int) {
    BluePatitasStepIndicator(
        currentStep = currentStep,
        steps = listOf(
            stringResource(R.string.shelter_step_basic),
            stringResource(R.string.shelter_step_location),
            stringResource(R.string.shelter_step_confirmation)
        )
    )
}

@Composable
private fun AuthFieldErrorBridge.asString(): String =
    when (this) {
        AuthFieldErrorBridge.Required -> stringResource(R.string.required_field)
        AuthFieldErrorBridge.InvalidEmail -> stringResource(R.string.invalid_email)
        AuthFieldErrorBridge.InvalidPhoneLength -> stringResource(R.string.phone_length_error)
        AuthFieldErrorBridge.InvalidTaxIdLength -> stringResource(R.string.tax_id_length_error)
    }
