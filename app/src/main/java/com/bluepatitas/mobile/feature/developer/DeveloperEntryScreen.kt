package com.bluepatitas.mobile.feature.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPrimaryButton
import com.bluepatitas.mobile.core.designsystem.icons.PawLogoIcon
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BluePatitasTheme
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurface
import com.bluepatitas.mobile.domain.model.AppLanguage

@Composable
fun DeveloperEntryScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onEnterAsAdministrator: () -> Unit,
    onEnterAsVeterinarian: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlueSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PawLogoIcon(
            modifier = Modifier.size(96.dp),
            color = BlueDark
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.demo_access_title),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.product_name),
            color = BlueDark,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.product_tagline),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        BluePatitasPrimaryButton(
            text = stringResource(R.string.enter_as_administrator),
            onClick = onEnterAsAdministrator
        )
        Spacer(modifier = Modifier.height(12.dp))
        BluePatitasOutlinedButton(
            text = stringResource(R.string.enter_as_veterinarian),
            onClick = onEnterAsVeterinarian
        )
        Spacer(modifier = Modifier.height(24.dp))
        LanguageSelector(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.development_mode_notice),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.language_label),
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
                label = { Text(text = stringResource(R.string.language_english)) }
            )
            FilterChip(
                selected = selectedLanguage == AppLanguage.SPANISH_LATAM,
                onClick = { onLanguageSelected(AppLanguage.SPANISH_LATAM) },
                label = { Text(text = stringResource(R.string.language_spanish)) }
            )
        }
    }
}

@Preview
@Composable
private fun DeveloperEntryPreview() {
    BluePatitasTheme {
        DeveloperEntryScreen(
            selectedLanguage = AppLanguage.ENGLISH,
            onLanguageSelected = {},
            onEnterAsAdministrator = {},
            onEnterAsVeterinarian = {}
        )
    }
}
