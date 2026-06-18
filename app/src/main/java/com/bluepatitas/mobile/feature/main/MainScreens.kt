package com.bluepatitas.mobile.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPrimaryButton
import com.bluepatitas.mobile.core.designsystem.components.ErrorContent
import com.bluepatitas.mobile.core.designsystem.components.LoadingContent
import com.bluepatitas.mobile.core.designsystem.theme.AmberWarning
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BluePrimary
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurface
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.core.designsystem.theme.RedCritical
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.UserRole

@Composable
fun AdminHomeRoute(
    session: AppSession,
    onOpenAnimals: () -> Unit,
    onOpenMonitoring: () -> Unit,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainDataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainSurface(modifier = modifier) {
        if (state.isLoading) {
            LoadingContent()
        } else {
            DashboardContent(
                session = session,
                state = state,
                onOpenAnimals = onOpenAnimals,
                onOpenMonitoring = onOpenMonitoring,
                onOpenAlerts = onOpenAlerts,
                onRetry = viewModel::refresh
            )
        }
    }
}

@Composable
fun AdminAnimalsRoute(
    modifier: Modifier = Modifier,
    viewModel: MainDataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainSurface(modifier = modifier) {
        if (state.isLoading) {
            LoadingContent()
        } else {
            AnimalsContent(
                title = stringResource(R.string.animals),
                subtitle = stringResource(R.string.admin_animals_subtitle),
                animals = state.animals,
                usingFallback = state.usingFallbackAnimals,
                onRetry = viewModel::refresh
            )
        }
    }
}

@Composable
fun MonitoringRoute(
    session: AppSession,
    modifier: Modifier = Modifier,
    viewModel: MainDataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var phoneCameraActive by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.clearCameraPermissionDenied()
            phoneCameraActive = true
        } else {
            viewModel.markCameraPermissionDenied()
        }
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) viewModel.markNotificationPermissionDenied()
        viewModel.simulateBreach(session)
    }

    fun activateCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModel.clearCameraPermissionDenied()
            phoneCameraActive = true
        } else {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun simulateBreach() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.simulateBreach(session)
        }
    }

    MainSurface(modifier = modifier) {
        if (state.isLoading) {
            LoadingContent()
        } else {
            MonitoringContent(
                session = session,
                state = state,
                phoneCameraActive = phoneCameraActive,
                onActivateCamera = ::activateCamera,
                onStopCamera = { phoneCameraActive = false },
                onSimulateBreach = ::simulateBreach,
                onSimulateSafe = viewModel::simulateSafePosition,
                onSelectZone = viewModel::selectZone,
                onRetry = viewModel::refresh
            )
        }
    }
}

@Composable
fun AlertsRoute(
    session: AppSession,
    modifier: Modifier = Modifier,
    viewModel: MainDataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainSurface(modifier = modifier) {
        if (state.isLoading) {
            LoadingContent()
        } else {
            AlertsContent(
                session = session,
                alerts = state.alerts,
                usingFallback = state.usingFallbackMonitoring,
                onRetry = viewModel::refresh,
                onResolve = viewModel::resolveAlert
            )
        }
    }
}

@Composable
fun ProfileRoute(
    session: AppSession,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    MainSurface(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScreenHeader(
                    title = stringResource(R.string.profile),
                    subtitle = stringResource(R.string.profile_subtitle)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialAvatar(text = session.displayName)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(session.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(session.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        InfoRow(stringResource(R.string.current_role), roleLabel(session.role))
                        InfoRow(stringResource(R.string.shelter), session.shelterName ?: stringResource(R.string.not_available))
                        InfoRow(stringResource(R.string.session_status), stringResource(R.string.active))
                    }
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
}

@Composable
private fun DashboardContent(
    session: AppSession,
    state: MainDataUiState,
    onOpenAnimals: () -> Unit,
    onOpenMonitoring: () -> Unit,
    onOpenAlerts: () -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.dashboard_welcome, session.displayName.firstName()),
                subtitle = session.shelterName ?: stringResource(R.string.shelter_overview)
            )
        }
        if (state.errorMessageVisible) {
            item { FallbackBanner(onRetry = onRetry) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(stringResource(R.string.animals_registered), state.animals.size.toString(), Modifier.weight(1f))
                MetricTile(stringResource(R.string.active_alerts), state.alerts.size.toString(), Modifier.weight(1f), critical = state.alerts.isNotEmpty())
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(stringResource(R.string.zones_monitored), state.zones.size.toString(), Modifier.weight(1f))
                MetricTile(stringResource(R.string.devices), state.zones.count { it.cameraEnabled }.toString(), Modifier.weight(1f))
            }
        }
        item {
            SectionTitle(stringResource(R.string.quick_actions))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(stringResource(R.string.animals), onOpenAnimals, Modifier.weight(1f))
                QuickAction(stringResource(R.string.monitoring), onOpenMonitoring, Modifier.weight(1f))
                QuickAction(stringResource(R.string.alerts), onOpenAlerts, Modifier.weight(1f))
            }
        }
        item {
            SectionTitle(stringResource(R.string.alerts_recent))
            if (state.alerts.isEmpty()) {
                EmptyPanel(stringResource(R.string.no_active_alerts))
            } else {
                AlertCard(alert = state.alerts.first(), onResolve = null)
            }
        }
        item {
            SectionTitle(stringResource(R.string.environment_by_zone))
        }
        items(state.zones.take(3)) { zone ->
            ZoneCompactCard(zone = zone)
        }
    }
}

@Composable
private fun AnimalsContent(
    title: String,
    subtitle: String,
    animals: List<AnimalSummary>,
    usingFallback: Boolean,
    onRetry: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(title = title, subtitle = subtitle)
        }
        if (usingFallback) item { FallbackBanner(onRetry = onRetry) }
        if (animals.isEmpty()) {
            item { EmptyPanel(stringResource(R.string.no_animals_available)) }
        } else {
            items(animals) { animal ->
                AnimalSummaryCard(animal)
            }
        }
    }
}

@Composable
private fun MonitoringContent(
    session: AppSession,
    state: MainDataUiState,
    phoneCameraActive: Boolean,
    onActivateCamera: () -> Unit,
    onStopCamera: () -> Unit,
    onSimulateBreach: () -> Unit,
    onSimulateSafe: () -> Unit,
    onSelectZone: (MonitoringZone) -> Unit,
    onRetry: () -> Unit
) {
    val selectedZone = state.selectedZone ?: state.zones.firstOrNull()
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = if (session.role == UserRole.VETERINARIAN) {
                    stringResource(R.string.animal_monitoring)
                } else {
                    stringResource(R.string.monitoring)
                },
                subtitle = stringResource(R.string.monitoring_subtitle)
            )
        }
        if (state.usingFallbackMonitoring) item { FallbackBanner(onRetry = onRetry) }
        if (state.cameraPermissionDenied) {
            item { WarningPanel(stringResource(R.string.camera_permission_required)) }
        }
        if (state.notificationPermissionDenied) {
            item { WarningPanel(stringResource(R.string.notifications_disabled_message)) }
        }
        items(state.zones) { zone ->
            MonitoringZoneCard(
                zone = zone,
                selected = selectedZone?.id == zone.id,
                onClick = { onSelectZone(zone) }
            )
        }
        selectedZone?.let { zone ->
            item {
                ZoneDetailCard(
                    zone = zone,
                    geofenceStatus = state.geofenceStatus,
                    phoneCameraActive = phoneCameraActive,
                    onActivateCamera = onActivateCamera,
                    onStopCamera = onStopCamera,
                    onSimulateBreach = onSimulateBreach,
                    onSimulateSafe = onSimulateSafe
                )
            }
        }
    }
}

@Composable
private fun AlertsContent(
    session: AppSession,
    alerts: List<MonitoringAlert>,
    usingFallback: Boolean,
    onRetry: () -> Unit,
    onResolve: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.alerts),
                subtitle = if (session.role == UserRole.VETERINARIAN) {
                    stringResource(R.string.vet_alerts_subtitle)
                } else {
                    stringResource(R.string.admin_alerts_subtitle)
                }
            )
        }
        if (usingFallback) item { FallbackBanner(onRetry = onRetry) }
        if (alerts.isEmpty()) {
            item {
                EmptyPanel(
                    if (session.role == UserRole.VETERINARIAN) {
                        stringResource(R.string.no_vet_alerts)
                    } else {
                        stringResource(R.string.no_active_alerts)
                    }
                )
            }
        } else {
            items(alerts) { alert ->
                AlertCard(alert = alert, onResolve = if (alert.isLocal) onResolve else null)
            }
        }
    }
}

@Composable
private fun MainSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBFF))
    ) {
        content()
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = BluePrimary,
            fontWeight = FontWeight.Bold
        )
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, critical: Boolean = false) {
    Card(
        modifier = modifier.height(112.dp),
        colors = CardDefaults.cardColors(containerColor = if (critical) Color(0xFFFFF3F3) else Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = if (critical) RedCritical else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineLarge, color = if (critical) RedCritical else BlueDark)
        }
    }
}

@Composable
private fun QuickAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(48.dp), shape = RoundedCornerShape(14.dp)) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BlueDark)
}

@Composable
private fun AnimalSummaryCard(animal: AnimalSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialAvatar(text = animal.name)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(animal.name.ifBlank { stringResource(R.string.unknown_animal) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${animal.species.ifBlank { stringResource(R.string.not_available) }} • ${animal.breed ?: stringResource(R.string.not_available)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(
                        R.string.animal_weight_zone,
                        animal.weightKg?.toString() ?: stringResource(R.string.not_available),
                        animal.zoneName ?: stringResource(R.string.not_available)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(text = animal.healthCondition ?: stringResource(R.string.normal), critical = animal.healthCondition?.contains("TREAT", true) == true)
        }
    }
}

@Composable
private fun MonitoringZoneCard(zone: MonitoringZone, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (selected) BlueSurface else Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.zone_camera_status, if (zone.cameraEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(zone.status.ifBlank { stringResource(R.string.normal) }, critical = zone.status.contains("alert", true))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(stringResource(R.string.temperature_short), "${zone.temperatureC?.toInt() ?: 0}°C", Modifier.weight(1f))
                SmallMetric(stringResource(R.string.humidity_short), "${zone.humidity?.toInt() ?: 0}%", Modifier.weight(1f))
                SmallMetric(stringResource(R.string.animals), zone.animalCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ZoneDetailCard(
    zone: MonitoringZone,
    geofenceStatus: GeofenceStatus,
    phoneCameraActive: Boolean,
    onActivateCamera: () -> Unit,
    onStopCamera: () -> Unit,
    onSimulateBreach: () -> Unit,
    onSimulateSafe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.zone_detail_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
                StatusPill(if (geofenceStatus == GeofenceStatus.OutsideSafeZone) stringResource(R.string.outside_safe_zone) else stringResource(R.string.inside_safe_zone), critical = geofenceStatus == GeofenceStatus.OutsideSafeZone)
            }
            if (phoneCameraActive) {
                PhoneCameraPreview()
            } else {
                CameraPlaceholder(imageUrl = zone.imageUrl)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(stringResource(R.string.min_temp), "${zone.minTemperatureC?.toInt() ?: 0}°C", Modifier.weight(1f))
                SmallMetric(stringResource(R.string.max_temp), "${zone.maxTemperatureC?.toInt() ?: 0}°C", Modifier.weight(1f))
            }
            if (geofenceStatus == GeofenceStatus.OutsideSafeZone) {
                WarningPanel(stringResource(R.string.pet_left_safe_zone))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (phoneCameraActive) {
                    OutlinedButton(onClick = onStopCamera, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.stop_camera))
                    }
                } else {
                    Button(onClick = onActivateCamera, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.activate_phone_camera))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSimulateSafe, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.simulate_safe_position))
                }
                Button(onClick = onSimulateBreach, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.simulate_breach))
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: MonitoringAlert, onResolve: ((String) -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (alert.isBreachConfirmed) Color(0xFFFFF3F3) else Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(alert.zoneName.ifBlank { stringResource(R.string.monitoring) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(alert.message, style = MaterialTheme.typography.bodyMedium)
                }
                StatusPill(if (alert.isLocal) stringResource(R.string.local) else stringResource(R.string.backend), critical = alert.isBreachConfirmed)
            }
            if (alert.latitude != null && alert.longitude != null) {
                Text(
                    stringResource(R.string.coordinates_value, alert.latitude, alert.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                if (alert.trackingActive) stringResource(R.string.tracking_active) else stringResource(R.string.tracking_inactive),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            onResolve?.let {
                BluePatitasOutlinedButton(text = stringResource(R.string.resolve_alert), onClick = { it(alert.id) })
            }
        }
    }
}

@Composable
private fun CameraPlaceholder(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFDDECF7)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = imageUrl?.takeIf { it.isNotBlank() } ?: stringResource(R.string.camera_snapshot_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = BlueDark
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun PhoneCameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { PreviewView(it).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } },
        update = { previewView ->
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            }
        }
    )
    DisposableEffect(Unit) {
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }
}

@Composable
private fun FallbackBanner(onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.simulated_for_presentation), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun WarningPanel(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF3F3),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(14.dp), color = RedCritical, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyPanel(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = BlueSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, color = BlueDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusPill(text: String, critical: Boolean = false) {
    val color = when {
        critical -> RedCritical
        text.contains("warning", true) -> AmberWarning
        else -> GreenSuccess
    }
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(100.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ZoneCompactCard(zone: MonitoringZone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.zone_environment_value, zone.temperatureC?.toInt() ?: 0, zone.humidity?.toInt() ?: 0),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusPill(zone.status.ifBlank { stringResource(R.string.normal) }, critical = zone.status.contains("alert", true))
        }
    }
}

@Composable
private fun InitialAvatar(text: String) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(BluePrimary),
        contentAlignment = Alignment.Center
    ) {
        Text(text.initials(), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = BlueDark)
    }
}

@Composable
private fun roleLabel(role: UserRole): String =
    when (role) {
        UserRole.SHELTER_ADMIN -> stringResource(R.string.role_shelter_admin)
        UserRole.VETERINARIAN -> stringResource(R.string.role_veterinarian)
    }

private fun String.initials(): String =
    trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "BP" }

private fun String.firstName(): String = trim().substringBefore(" ").ifBlank { this }
