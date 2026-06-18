package com.bluepatitas.mobile.feature.veterinary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.ErrorContent
import com.bluepatitas.mobile.core.designsystem.components.LoadingContent
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.RecentObservation
import com.bluepatitas.mobile.domain.model.VeterinaryAnimal
import com.bluepatitas.mobile.domain.model.VeterinaryDashboard

@Composable
fun VeterinaryDashboardRoute(
    session: AppSession,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VeterinaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingContent(modifier = modifier.fillMaxSize())
        uiState.hasError -> VeterinaryErrorContent(
            onRetry = viewModel::loadDashboard,
            onSignOut = onSignOut,
            modifier = modifier.fillMaxSize()
        )

        uiState.dashboard != null -> VeterinaryDashboardScreen(
            dashboard = uiState.dashboard,
            session = session,
            onSignOut = onSignOut,
            modifier = modifier
        )
    }
}

@Composable
fun VeterinaryAnimalsRoute(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VeterinaryAnimalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingContent(modifier = modifier.fillMaxSize())
        uiState.hasError -> VeterinaryErrorContent(
            onRetry = viewModel::loadAnimals,
            onSignOut = onSignOut,
            modifier = modifier.fillMaxSize()
        )

        else -> VeterinaryAnimalsScreen(
            animals = uiState.animals,
            onSignOut = onSignOut,
            modifier = modifier
        )
    }
}

@Composable
private fun VeterinaryDashboardScreen(
    dashboard: VeterinaryDashboard?,
    session: AppSession,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val veterinarianName = dashboard?.veterinarianName
        ?.takeIf { it.isNotBlank() }
        ?: session.displayName
    val shelterName = dashboard?.shelterName
        ?.takeIf { it.isNotBlank() }
        ?: session.shelterName.orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.veterinary_dashboard_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = veterinarianName, style = MaterialTheme.typography.titleMedium)
                if (shelterName.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.shelter_value, shelterName),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = stringResource(R.string.animals_under_care),
                    value = dashboard?.animalsUnderCare ?: 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = stringResource(R.string.pending_observations),
                    value = dashboard?.pendingObservations ?: 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = stringResource(R.string.active_alerts),
                    value = dashboard?.activeAlerts ?: 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = stringResource(R.string.recent_observations),
                    value = dashboard?.recentObservationsCount ?: 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.recent_observations),
                style = MaterialTheme.typography.titleLarge
            )
        }
        val observations = dashboard?.recentObservations.orEmpty()
        if (observations.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_recent_observations),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(observations) { observation ->
                ObservationCard(observation = observation)
            }
        }
        item {
            BluePatitasOutlinedButton(
                text = stringResource(R.string.sign_out),
                onClick = onSignOut
            )
        }
    }
}

@Composable
private fun VeterinaryAnimalsScreen(
    animals: List<VeterinaryAnimal>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.my_shelter_animals),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (animals.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_veterinary_animals),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(animals) { animal ->
                AnimalCard(animal = animal)
            }
        }
        item {
            BluePatitasOutlinedButton(
                text = stringResource(R.string.sign_out),
                onClick = onSignOut
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ObservationCard(
    observation: RecentObservation,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = observation.animalName.ifBlank { stringResource(R.string.unknown_animal) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = observation.description.ifBlank { stringResource(R.string.no_observation_detail) },
                style = MaterialTheme.typography.bodyMedium
            )
            if (observation.createdAt.isNotBlank()) {
                Text(text = observation.createdAt, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun AnimalCard(
    animal: VeterinaryAnimal,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = animal.name.ifBlank { stringResource(R.string.unknown_animal) },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            InfoLine(label = stringResource(R.string.species), value = animal.species)
            animal.breed?.takeIf { it.isNotBlank() }?.let {
                InfoLine(label = stringResource(R.string.breed), value = it)
            }
            animal.healthCondition?.takeIf { it.isNotBlank() }?.let {
                InfoLine(label = stringResource(R.string.health_condition), value = it)
            }
            animal.weightKg?.let {
                InfoLine(label = stringResource(R.string.weight_kg), value = it.toString())
            }
            animal.photoUrl?.takeIf { it.isNotBlank() }?.let {
                InfoLine(label = stringResource(R.string.photo_url), value = it)
            }
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.label_value, label, value.ifBlank { stringResource(R.string.not_available) }),
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun VeterinaryErrorContent(
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ErrorContent(
            title = stringResource(R.string.veterinary_load_error_title),
            message = stringResource(R.string.veterinary_load_error_message)
        )
        BluePatitasOutlinedButton(
            text = stringResource(R.string.retry),
            onClick = onRetry
        )
        BluePatitasOutlinedButton(
            text = stringResource(R.string.sign_out),
            onClick = onSignOut
        )
    }
}
