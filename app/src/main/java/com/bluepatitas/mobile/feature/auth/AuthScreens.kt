package com.bluepatitas.mobile.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasAuthHeader
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasCheckboxRow
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasFormScaffold
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasInfoCard
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasLanguageSelector
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPasswordField
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPrimaryButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasTextField
import com.bluepatitas.mobile.core.designsystem.icons.PawLogoIcon
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurface
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurfaceVariant
import com.bluepatitas.mobile.core.designsystem.theme.Spacing
import com.bluepatitas.mobile.domain.model.AppLanguage
import kotlinx.coroutines.delay

@Composable
fun activeLanguageFromResources(): AppLanguage {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        AppLanguage.fromTag(configuration.locales[0].toLanguageTag())
    }
}

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    navigationKey: Any = Unit
) {
    val logoDescription = stringResource(R.string.bluepatitas_logo_description)
    LaunchedEffect(navigationKey) {
        delay(900)
        onFinished()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PawLogoIcon(
                color = BlueDark,
                modifier = Modifier.semantics {
                    contentDescription = logoDescription
                }
            )
            Text(
                text = stringResource(R.string.product_name),
                style = MaterialTheme.typography.headlineLarge,
                color = BlueDark
            )
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    onInvitation: () -> Unit,
    onDeveloperAccess: () -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val illustrationDescription = stringResource(R.string.shelter_iot_illustration_description)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlueSurface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.product_name), style = MaterialTheme.typography.titleLarge, color = BlueDark)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .semantics {
                    contentDescription = illustrationDescription
                },
            colors = CardDefaults.cardColors(containerColor = BlueSurfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PawLogoIcon(color = BlueDark, modifier = Modifier.size(92.dp))
            }
        }
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = BlueDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.welcome_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BluePatitasInfoCard(title = stringResource(R.string.benefit_realtime_monitoring))
            BluePatitasInfoCard(title = stringResource(R.string.benefit_critical_alerts))
            BluePatitasInfoCard(title = stringResource(R.string.benefit_automated_feeding))
        }
        BluePatitasPrimaryButton(text = stringResource(R.string.get_started), onClick = onGetStarted)
        BluePatitasOutlinedButton(text = stringResource(R.string.already_have_account), onClick = onLogin)
        TextButton(onClick = onInvitation) {
            Text(text = stringResource(R.string.accept_invitation))
        }
        BluePatitasLanguageSelector(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected
        )
        TextButton(onClick = onDeveloperAccess) {
            Text(text = stringResource(R.string.development_access), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun LoginRoute(
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onAdminOnboarding: () -> Unit,
    onAdminMain: () -> Unit,
    onVeterinarianMain: () -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.destination) {
        when (state.destination) {
            LoginDestination.AdminOnboarding -> {
                viewModel.clearDestination()
                onAdminOnboarding()
            }

            LoginDestination.AdminMain -> {
                viewModel.clearDestination()
                onAdminMain()
            }

            LoginDestination.VeterinarianMain -> {
                viewModel.clearDestination()
                onVeterinarianMain()
            }

            null -> Unit
        }
    }
    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityChange = viewModel::onPasswordVisibilityChange,
        onSubmit = viewModel::submit,
        onForgotPassword = onForgotPassword,
        onRegister = onRegister,
        selectedLanguage = selectedLanguage,
        onLanguageSelected = onLanguageSelected
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.welcome_back),
        subtitle = stringResource(R.string.login_subtitle),
        bottomContent = {
            BluePatitasLanguageSelector(selectedLanguage, onLanguageSelected)
        }
    ) {
        BluePatitasTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.email_address),
            error = state.emailError?.asString(),
            keyboardType = KeyboardType.Email
        )
        BluePatitasPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.password),
            visible = state.passwordVisible,
            onVisibilityChange = onPasswordVisibilityChange,
            error = state.passwordError?.asString()
        )
        TextButton(onClick = onForgotPassword, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.forgot_password))
        }
        if (state.formError != null) {
            Text(text = state.formError.asString(), color = MaterialTheme.colorScheme.error)
        }
        BluePatitasPrimaryButton(
            text = stringResource(R.string.sign_in),
            onClick = onSubmit,
            enabled = !state.isSubmitting
        )
        TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.create_admin_account))
        }
    }
}

@Composable
fun RegisterRoute(
    onCompleted: () -> Unit,
    onLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.completed) {
        if (state.completed) {
            viewModel.clearCompleted()
            onCompleted()
        }
    }
    RegisterScreen(
        state = state,
        onFieldChange = viewModel::updateField,
        onPasswordVisible = viewModel::setPasswordVisible,
        onConfirmPasswordVisible = viewModel::setConfirmPasswordVisible,
        onAcceptedTerms = viewModel::setAcceptedTerms,
        onSubmit = viewModel::submit,
        onLogin = onLogin
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onFieldChange: (String, String) -> Unit,
    onPasswordVisible: (Boolean) -> Unit,
    onConfirmPasswordVisible: (Boolean) -> Unit,
    onAcceptedTerms: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onLogin: () -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.register_title),
        subtitle = stringResource(R.string.register_subtitle)
    ) {
        BluePatitasTextField(state.firstName, { onFieldChange("firstName", it) }, stringResource(R.string.first_name), error = state.errors["firstName"]?.asString())
        BluePatitasTextField(state.lastName, { onFieldChange("lastName", it) }, stringResource(R.string.last_name), error = state.errors["lastName"]?.asString())
        BluePatitasTextField(state.email, { onFieldChange("email", it) }, stringResource(R.string.email_address), error = state.errors["email"]?.asString(), keyboardType = KeyboardType.Email)
        BluePatitasTextField(state.phone, { onFieldChange("phone", it) }, stringResource(R.string.phone_number), error = state.errors["phone"]?.asString(), keyboardType = KeyboardType.Phone)
        BluePatitasPasswordField(state.password, { onFieldChange("password", it) }, stringResource(R.string.password), state.passwordVisible, onPasswordVisible, error = state.errors["password"]?.asString())
        BluePatitasPasswordField(state.confirmPassword, { onFieldChange("confirmPassword", it) }, stringResource(R.string.confirm_password), state.confirmPasswordVisible, onConfirmPasswordVisible, error = state.errors["confirmPassword"]?.asString())
        BluePatitasCheckboxRow(state.acceptedTerms, onAcceptedTerms, stringResource(R.string.accept_terms), error = state.errors["terms"]?.asString())
        BluePatitasPrimaryButton(text = stringResource(R.string.create_account), onClick = onSubmit, enabled = !state.isSubmitting)
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.have_account_sign_in))
        }
    }
}

@Composable
fun ForgotPasswordRoute(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ForgotPasswordScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::submit,
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.forgot_password_title),
        subtitle = stringResource(R.string.forgot_password_subtitle)
    ) {
        BluePatitasTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.email_address),
            error = state.emailError?.asString(),
            keyboardType = KeyboardType.Email
        )
        if (state.success) {
            Text(text = stringResource(R.string.reset_success), color = MaterialTheme.colorScheme.secondary)
        }
        BluePatitasPrimaryButton(text = stringResource(R.string.send_reset_link), onClick = onSubmit)
        BluePatitasOutlinedButton(text = stringResource(R.string.back_to_login), onClick = onBackToLogin)
    }
}

@Composable
fun InvitationRoute(
    onCompleted: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.completed) {
        if (state.completed) {
            viewModel.clearCompleted()
            onCompleted()
        }
    }
    InvitationScreen(
        state = state,
        onFieldChange = viewModel::updateField,
        onPasswordVisible = viewModel::setPasswordVisible,
        onSubmit = viewModel::submit
    )
}

@Composable
fun InvitationScreen(
    state: InvitationUiState,
    onFieldChange: (String, String) -> Unit,
    onPasswordVisible: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    BluePatitasFormScaffold(
        title = stringResource(R.string.invitation_title),
        subtitle = stringResource(R.string.invitation_subtitle)
    ) {
        BluePatitasTextField(state.code, { onFieldChange("code", it) }, stringResource(R.string.invitation_code), error = state.errors["code"]?.asString())
        BluePatitasTextField(state.email, { onFieldChange("email", it) }, stringResource(R.string.email_address), error = state.errors["email"]?.asString(), keyboardType = KeyboardType.Email)
        BluePatitasPasswordField(state.password, { onFieldChange("password", it) }, stringResource(R.string.password), state.passwordVisible, onPasswordVisible, error = state.errors["password"]?.asString())
        BluePatitasPrimaryButton(text = stringResource(R.string.join_shelter), onClick = onSubmit, enabled = !state.isSubmitting)
    }
}

@Composable
fun AuthFieldError.asString(): String =
    when (this) {
        AuthFieldError.Required -> stringResource(R.string.required_field)
        AuthFieldError.InvalidEmail -> stringResource(R.string.invalid_email)
        AuthFieldError.PasswordRequired -> stringResource(R.string.password_required)
        AuthFieldError.PasswordTooShort -> stringResource(R.string.password_too_short)
        AuthFieldError.PasswordsDoNotMatch -> stringResource(R.string.passwords_do_not_match)
        AuthFieldError.TermsRequired -> stringResource(R.string.terms_required)
        AuthFieldError.InvalidCredentials -> stringResource(R.string.invalid_credentials)
        AuthFieldError.InvalidInvitationCode -> stringResource(R.string.invalid_invitation_code)
    }
