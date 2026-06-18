package com.bluepatitas.mobile.feature.veterinary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.ErrorContent
import com.bluepatitas.mobile.core.designsystem.components.LoadingContent
import com.bluepatitas.mobile.core.designsystem.theme.AmberWarning
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BluePrimary
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.core.designsystem.theme.RedCritical
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F7FD))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_welcome, veterinarianName.substringBefore(" ")),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BlueDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (shelterName.isNotBlank()) stringResource(R.string.shelter_value, shelterName) else stringResource(R.string.shelter_overview),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5C6B7A),
                        fontWeight = FontWeight.Medium
                    )
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BlueDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            val observations = dashboard?.recentObservations.orEmpty()
            if (observations.isEmpty()) {
                item {
                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EFF5)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.no_recent_observations),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5C6B7A),
                            modifier = Modifier.padding(18.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(observations) { observation ->
                    ObservationCard(observation = observation)
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BluePatitasOutlinedButton(
                    text = stringResource(R.string.sign_out),
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun VeterinaryAnimalsScreen(
    animals: List<VeterinaryAnimal>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F7FD))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.my_shelter_animals),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BlueDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.admin_animals_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5C6B7A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (animals.isEmpty()) {
                item {
                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EFF5)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.no_veterinary_animals),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5C6B7A),
                            modifier = Modifier.padding(18.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(animals) { animal ->
                    AnimalCard(animal = animal)
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BluePatitasOutlinedButton(
                    text = stringResource(R.string.sign_out),
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(118.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF5C6B7A),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = BlueDark,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ObservationCard(
    observation: RecentObservation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = observation.animalName.ifBlank { stringResource(R.string.unknown_animal) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )
            Text(
                text = observation.description.ifBlank { stringResource(R.string.no_observation_detail) },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF102030)
            )
            if (observation.createdAt.isNotBlank()) {
                Text(
                    text = observation.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5C6B7A),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AnimalCard(
    animal: VeterinaryAnimal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialAvatar(text = animal.name)
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = animal.name.ifBlank { stringResource(R.string.unknown_animal) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueDark
                )
                Text(
                    text = "${animal.species.ifBlank { stringResource(R.string.not_available) }} • ${animal.breed?.ifBlank { stringResource(R.string.not_available) } ?: stringResource(R.string.not_available)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5C6B7A)
                )
                Text(
                    text = stringResource(
                        R.string.label_value,
                        stringResource(R.string.weight_kg),
                        animal.weightKg?.toString() ?: stringResource(R.string.not_available)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5C6B7A)
                )
            }
            StatusPill(
                text = animal.healthCondition?.ifBlank { stringResource(R.string.normal) } ?: stringResource(R.string.normal),
                critical = animal.healthCondition?.contains("TREAT", true) == true
            )
        }
    }
}

@Composable
private fun VeterinaryErrorContent(
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF2F7FD))
            .padding(24.dp),
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

@Composable
private fun InitialAvatar(text: String) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFEAF4FF))
            .border(2.dp, Color(0xFFD0E3FA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.initials(),
            color = BluePrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusPill(text: String, critical: Boolean = false) {
    val color = if (critical || text.contains("critical", true) || text.contains("alert", true)) RedCritical else GreenSuccess
    val bgAlpha = 0.12f
    val textLabel = text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    Surface(
        color = color.copy(alpha = bgAlpha),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = textLabel,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String.initials(): String =
    trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "BP" }
