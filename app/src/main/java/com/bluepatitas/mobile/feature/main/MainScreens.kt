package com.bluepatitas.mobile.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasPrimaryButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasTextField
import com.bluepatitas.mobile.core.designsystem.components.ErrorContent
import com.bluepatitas.mobile.core.designsystem.components.LoadingContent
import com.bluepatitas.mobile.core.designsystem.theme.AmberWarning
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BluePrimary
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurface
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.core.designsystem.theme.RedCritical
import com.bluepatitas.mobile.core.designsystem.theme.Ink
import com.bluepatitas.mobile.core.designsystem.theme.MutedInk
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.TelemetryRecord
import com.bluepatitas.mobile.domain.model.UserRole
import coil.compose.AsyncImage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private const val IOT_REFRESH_INTERVAL_MILLIS = 15_000L

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
                state = state,
                onRetry = viewModel::refresh,
                onShowRegisterAnimal = viewModel::showRegisterAnimalForm,
                onDismissRegisterAnimal = viewModel::hideRegisterAnimalForm,
                onAnimalFormChange = viewModel::updateAnimalForm,
                onSelectAnimalImage = viewModel::selectAnimalImage,
                onClearAnimalImage = viewModel::clearAnimalImage,
                onCreateAnimal = viewModel::createAnimal,
                onDismissCreatedMessage = viewModel::dismissAnimalCreatedMessage,
                onOpenAnimalDetail = viewModel::openAnimalDetail,
                onRetryAnimalDetail = viewModel::retryAnimalDetail,
                onShowHealthEditor = viewModel::showHealthEditor,
                onHideHealthEditor = viewModel::hideHealthEditor,
                onSelectHealthCondition = viewModel::selectHealthCondition,
                onUpdateAnimalHealth = viewModel::updateAnimalHealth,
                onDismissHealthUpdatedMessage = viewModel::dismissAnimalHealthUpdatedMessage,
                onShowZoneAssignmentEditor = viewModel::showZoneAssignmentEditor,
                onHideZoneAssignmentEditor = viewModel::hideZoneAssignmentEditor,
                onSelectPerimeter = viewModel::selectPerimeter,
                onAssignAnimalToZone = viewModel::assignAnimalToSelectedZone,
                onRemoveAnimalZone = viewModel::removeAnimalZoneAssignment,
                onDismissZoneAssignedMessage = viewModel::dismissAnimalZoneAssignedMessage,
                onCloseAnimalDetail = viewModel::closeAnimalDetail
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

    LaunchedEffect(viewModel) {
        while (true) {
            delay(IOT_REFRESH_INTERVAL_MILLIS)
            viewModel.refreshMonitoring()
        }
    }

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
                onShowCreateZone = viewModel::showCreateZoneForm,
                onDismissCreateZone = viewModel::hideCreateZoneForm,
                onZoneFormChange = viewModel::updateZoneForm,
                onZoneCameraEnabledChange = viewModel::updateZoneCameraEnabled,
                onCreateZone = viewModel::createMonitoringZone,
                onDismissZoneCreatedMessage = viewModel::dismissZoneCreatedMessage,
                onRefreshMonitoring = viewModel::refreshMonitoring
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
    LaunchedEffect(viewModel) {
        while (true) {
            delay(IOT_REFRESH_INTERVAL_MILLIS)
            viewModel.refreshAlerts()
        }
    }
    MainSurface(modifier = modifier) {
        if (state.isLoading) {
            LoadingContent()
        } else {
            AlertsContent(
                session = session,
                alerts = state.alerts,
                usingFallback = state.usingFallbackMonitoring,
                loadError = state.monitoringLoadError,
                actionError = state.monitoringActionError,
                resolvingAlertId = state.isResolvingAlertId,
                enablingTrackingAlertId = state.isEnablingTrackingAlertId,
                isRefreshing = state.isRefreshingAlerts,
                updated = state.lastAlertsRefreshMillis != null,
                onRetry = viewModel::refreshAlerts,
                onResolve = viewModel::resolveAlert,
                onEnableTracking = viewModel::enableAlertTracking
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
                MetricTile(
                    stringResource(R.string.animals_registered),
                    if (state.usingFallbackAnimals) stringResource(R.string.demo_metric_value) else state.animals.size.toString(),
                    Modifier.weight(1f)
                )
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
                AlertCard(
                    alert = state.alerts.first(),
                    resolving = false,
                    enablingTracking = false,
                    onResolve = {},
                    onEnableTracking = {}
                )
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
    state: MainDataUiState,
    onRetry: () -> Unit,
    onShowRegisterAnimal: () -> Unit,
    onDismissRegisterAnimal: () -> Unit,
    onAnimalFormChange: (String, String) -> Unit,
    onSelectAnimalImage: (String?) -> Unit,
    onClearAnimalImage: () -> Unit,
    onCreateAnimal: () -> Unit,
    onDismissCreatedMessage: () -> Unit,
    onOpenAnimalDetail: (AnimalSummary) -> Unit,
    onRetryAnimalDetail: () -> Unit,
    onShowHealthEditor: () -> Unit,
    onHideHealthEditor: () -> Unit,
    onSelectHealthCondition: (String) -> Unit,
    onUpdateAnimalHealth: () -> Unit,
    onDismissHealthUpdatedMessage: () -> Unit,
    onShowZoneAssignmentEditor: () -> Unit,
    onHideZoneAssignmentEditor: () -> Unit,
    onSelectPerimeter: (String) -> Unit,
    onAssignAnimalToZone: () -> Unit,
    onRemoveAnimalZone: () -> Unit,
    onDismissZoneAssignedMessage: () -> Unit,
    onCloseAnimalDetail: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScreenHeader(title = title, subtitle = subtitle)
            }
            item {
                BluePatitasPrimaryButton(
                    text = stringResource(R.string.register_animal),
                    onClick = onShowRegisterAnimal
                )
            }
            if (state.animalCreatedMessageVisible) {
                item {
                    SuccessPanel(
                        text = stringResource(R.string.animal_created_success),
                        onDismiss = onDismissCreatedMessage
                    )
                }
            }
            if (state.animalLoadError != null) {
                item {
                    AnimalDataBanner(
                        error = state.animalLoadError,
                        usingFallback = state.usingFallbackAnimals,
                        onRetry = onRetry
                    )
                }
            }
            if (state.animals.isEmpty()) {
                item { EmptyAnimalPanel(onRegisterFirst = onShowRegisterAnimal) }
            } else {
                items(state.animals) { animal ->
                    AnimalSummaryCard(animal = animal, onClick = { onOpenAnimalDetail(animal) })
                }
            }
        }
        if (state.showRegisterAnimalForm) {
            RegisterAnimalDialog(
                form = state.animalForm,
                errors = state.animalFormErrors,
                actionError = state.animalActionError,
                isSaving = state.isSavingAnimal,
                onFieldChange = onAnimalFormChange,
                onSelectImage = onSelectAnimalImage,
                onClearImage = onClearAnimalImage,
                onCreate = onCreateAnimal,
                onDismiss = onDismissRegisterAnimal
            )
        }
        state.selectedAnimalDetail?.let { animal ->
            AnimalDetailDialog(
                animal = animal,
                isLoading = state.isLoadingAnimalDetail,
                detailError = state.animalDetailError,
                showHealthEditor = state.showHealthEditor,
                selectedHealthCondition = state.selectedHealthCondition,
                isUpdatingHealth = state.isUpdatingAnimalHealth,
                healthUpdateError = state.animalHealthUpdateError,
                healthUpdatedMessageVisible = state.animalHealthUpdatedMessageVisible,
                zones = state.zones,
                showZoneAssignmentEditor = state.showZoneAssignmentEditor,
                selectedPerimeterId = state.selectedPerimeterId,
                isAssigningZone = state.isAssigningAnimalZone,
                zoneAssignmentError = state.animalZoneAssignmentError,
                zoneAssignedMessageVisible = state.animalZoneAssignedMessageVisible,
                onRetry = onRetryAnimalDetail,
                onShowHealthEditor = onShowHealthEditor,
                onHideHealthEditor = onHideHealthEditor,
                onSelectHealthCondition = onSelectHealthCondition,
                onUpdateAnimalHealth = onUpdateAnimalHealth,
                onDismissHealthUpdatedMessage = onDismissHealthUpdatedMessage,
                onShowZoneAssignmentEditor = onShowZoneAssignmentEditor,
                onHideZoneAssignmentEditor = onHideZoneAssignmentEditor,
                onSelectPerimeter = onSelectPerimeter,
                onAssignAnimalToZone = onAssignAnimalToZone,
                onRemoveAnimalZone = onRemoveAnimalZone,
                onDismissZoneAssignedMessage = onDismissZoneAssignedMessage,
                onDismiss = onCloseAnimalDetail
            )
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
    onShowCreateZone: () -> Unit,
    onDismissCreateZone: () -> Unit,
    onZoneFormChange: (String, String) -> Unit,
    onZoneCameraEnabledChange: (Boolean) -> Unit,
    onCreateZone: () -> Unit,
    onDismissZoneCreatedMessage: () -> Unit,
    onRefreshMonitoring: () -> Unit
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
        item {
            BluePatitasPrimaryButton(
                text = stringResource(R.string.create_zone),
                onClick = onShowCreateZone
            )
        }
        item {
            RefreshStatusRow(
                isRefreshing = state.isRefreshingMonitoring,
                updated = state.lastMonitoringRefreshMillis != null,
                onRefresh = onRefreshMonitoring
            )
        }
        if (state.usingFallbackMonitoring) item { FallbackBanner(onRetry = onRefreshMonitoring) }
        state.monitoringLoadError?.takeIf { !state.usingFallbackMonitoring }?.let {
            item { WarningPanel(it.asMonitoringString()) }
        }
        state.monitoringActionError?.let {
            item { WarningPanel(it.asMonitoringString()) }
        }
        if (state.zoneCreatedMessageVisible) {
            item {
                SuccessPanel(
                    text = stringResource(R.string.zone_created_success),
                    onDismiss = onDismissZoneCreatedMessage
                )
            }
        }
        if (state.cameraPermissionDenied) {
            item { WarningPanel(stringResource(R.string.camera_permission_required)) }
        }
        if (state.notificationPermissionDenied) {
            item { WarningPanel(stringResource(R.string.notifications_disabled_message)) }
        }
        if (state.zones.isEmpty()) {
            item { EmptyPanel(stringResource(R.string.no_monitoring_zones)) }
        } else {
            items(state.zones) { zone ->
                MonitoringZoneCard(
                    zone = zone,
                    selected = selectedZone?.id == zone.id,
                    onClick = { onSelectZone(zone) }
                )
            }
        }
        selectedZone?.let { zone ->
            item {
                ZoneDetailCard(
                    zone = zone,
                    geofenceStatus = state.geofenceStatus,
                    phoneCameraActive = phoneCameraActive,
                    telemetry = state.selectedZoneTelemetry,
                    isLoadingTelemetry = state.isLoadingTelemetry,
                    telemetryError = state.telemetryError,
                    onActivateCamera = onActivateCamera,
                    onStopCamera = onStopCamera,
                    onSimulateBreach = onSimulateBreach,
                    onSimulateSafe = onSimulateSafe
                )
            }
        }
    }
    if (state.showCreateZoneForm) {
        CreateZoneDialog(
            form = state.zoneForm,
            errors = state.zoneFormErrors,
            isSaving = state.isSavingZone,
            actionError = state.monitoringActionError,
            onFieldChange = onZoneFormChange,
            onCameraEnabledChange = onZoneCameraEnabledChange,
            onCreate = onCreateZone,
            onDismiss = onDismissCreateZone
        )
    }
}

@Composable
private fun AlertsContent(
    session: AppSession,
    alerts: List<MonitoringAlert>,
    usingFallback: Boolean,
    loadError: AnimalActionError?,
    actionError: AnimalActionError?,
    resolvingAlertId: String?,
    enablingTrackingAlertId: String?,
    isRefreshing: Boolean,
    updated: Boolean,
    onRetry: () -> Unit,
    onResolve: (String) -> Unit,
    onEnableTracking: (MonitoringAlert) -> Unit
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
        item {
            RefreshStatusRow(
                isRefreshing = isRefreshing,
                updated = updated,
                onRefresh = onRetry
            )
        }
        if (usingFallback) item { FallbackBanner(onRetry = onRetry) }
        loadError?.takeIf { !usingFallback }?.let { item { WarningPanel(it.asMonitoringString()) } }
        actionError?.let { item { WarningPanel(it.asMonitoringString()) } }
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
                AlertCard(
                    alert = alert,
                    resolving = resolvingAlertId == alert.id,
                    enablingTracking = enablingTrackingAlertId == alert.id,
                    onResolve = onResolve,
                    onEnableTracking = onEnableTracking
                )
            }
        }
    }
}

@Composable
private fun RefreshStatusRow(
    isRefreshing: Boolean,
    updated: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when {
                isRefreshing -> stringResource(R.string.updating)
                updated -> stringResource(R.string.updated_just_now)
                else -> stringResource(R.string.iot_refresh_hint)
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        BluePatitasOutlinedButton(
            text = if (isRefreshing) stringResource(R.string.updating) else stringResource(R.string.refresh),
            onClick = onRefresh,
            modifier = Modifier.width(180.dp)
        )
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
            .background(Color(0xFFF2F7FD))
    ) {
        content()
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = BlueDark,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, critical: Boolean = false) {
    Card(
        modifier = modifier.height(118.dp),
        colors = CardDefaults.cardColors(containerColor = if (critical) Color(0xFFFFF4F4) else Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (critical) Color(0xFFFFC1C1) else Color(0xFFE9EFF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = if (critical) RedCritical else MutedInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = if (critical) RedCritical else BlueDark,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun QuickAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = BluePrimary,
            contentColor = Color.White
        ),
        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = BlueDark,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun AnimalSummaryCard(animal: AnimalSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimalPhotoPlaceholder(animal = animal, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = animal.name.ifBlank { stringResource(R.string.unknown_animal) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueDark
                )
                Text(
                    text = stringResource(
                        R.string.animal_species_breed,
                        animal.species.ifBlank { stringResource(R.string.not_available) },
                        animal.breed ?: stringResource(R.string.not_available)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
                Text(
                    text = stringResource(
                        R.string.animal_weight_zone,
                        animal.weightKg?.toString() ?: stringResource(R.string.not_available),
                        animal.zoneName
                            ?: if (animal.assignedPerimeterId != null) {
                                stringResource(R.string.assigned_zone)
                            } else {
                                stringResource(R.string.no_assigned_zone)
                            }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
            StatusPill(
                text = animal.healthCondition.healthConditionLabel(),
                critical = animal.healthCondition?.contains("CRITICAL", true) == true ||
                    animal.healthCondition?.contains("TREAT", true) == true
            )
        }
    }
}

@Composable
private fun RegisterAnimalDialog(
    form: AnimalFormUiState,
    errors: Map<String, AnimalFieldError>,
    actionError: AnimalActionError?,
    isSaving: Boolean,
    onFieldChange: (String, String) -> Unit,
    onSelectImage: (String?) -> Unit,
    onClearImage: () -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onSelectImage(uri?.toString())
    }
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScreenHeader(
                        title = stringResource(R.string.add_animal_title),
                        subtitle = stringResource(R.string.add_animal_subtitle)
                    )
                }
                item {
                    AnimalImagePicker(
                        form = form,
                        isSaving = isSaving,
                        onPickImage = { imagePicker.launch("image/*") },
                        onClearImage = onClearImage
                    )
                }
                actionError?.let {
                    item { WarningPanel(it.asString()) }
                }
                if (isSaving) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = BluePrimary,
                            trackColor = Color(0xFFDCEAF8)
                        )
                    }
                }
                item {
                    BluePatitasTextField(
                        value = form.name,
                        onValueChange = { onFieldChange("name", it) },
                        label = stringResource(R.string.animal_name),
                        error = errors["name"]?.asString()
                    )
                }
                item {
                    BluePatitasTextField(
                        value = form.species,
                        onValueChange = { onFieldChange("species", it) },
                        label = stringResource(R.string.species),
                        error = errors["species"]?.asString()
                    )
                }
                item {
                    BluePatitasTextField(
                        value = form.breed,
                        onValueChange = { onFieldChange("breed", it) },
                        label = stringResource(R.string.breed)
                    )
                }
                item {
                    BluePatitasTextField(
                        value = form.estimatedAgeMonths,
                        onValueChange = { onFieldChange("estimatedAgeMonths", it) },
                        label = stringResource(R.string.estimated_age_months),
                        error = errors["estimatedAgeMonths"]?.asString(),
                        keyboardType = KeyboardType.Number
                    )
                }
                item {
                    BluePatitasTextField(
                        value = form.weightKg,
                        onValueChange = { onFieldChange("weightKg", it) },
                        label = stringResource(R.string.weight_kg),
                        error = errors["weightKg"]?.asString(),
                        keyboardType = KeyboardType.Decimal
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BluePatitasOutlinedButton(
                            text = stringResource(R.string.cancel),
                            onClick = { if (!isSaving) onDismiss() },
                            modifier = Modifier.weight(1f)
                        )
                        BluePatitasPrimaryButton(
                            text = stringResource(R.string.save_animal),
                            onClick = onCreate,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimalDetailDialog(
    animal: AnimalSummary,
    isLoading: Boolean,
    detailError: AnimalActionError?,
    showHealthEditor: Boolean,
    selectedHealthCondition: String?,
    isUpdatingHealth: Boolean,
    healthUpdateError: AnimalActionError?,
    healthUpdatedMessageVisible: Boolean,
    zones: List<MonitoringZone>,
    showZoneAssignmentEditor: Boolean,
    selectedPerimeterId: String?,
    isAssigningZone: Boolean,
    zoneAssignmentError: AnimalActionError?,
    zoneAssignedMessageVisible: Boolean,
    onRetry: () -> Unit,
    onShowHealthEditor: () -> Unit,
    onHideHealthEditor: () -> Unit,
    onSelectHealthCondition: (String) -> Unit,
    onUpdateAnimalHealth: () -> Unit,
    onDismissHealthUpdatedMessage: () -> Unit,
    onShowZoneAssignmentEditor: () -> Unit,
    onHideZoneAssignmentEditor: () -> Unit,
    onSelectPerimeter: (String) -> Unit,
    onAssignAnimalToZone: () -> Unit,
    onRemoveAnimalZone: () -> Unit,
    onDismissZoneAssignedMessage: () -> Unit,
    onDismiss: () -> Unit
) {
    val assignedZone = animal.assignedPerimeterId?.let { perimeterId ->
        zones.firstOrNull { it.id == perimeterId }
    }
    val assignedZoneLabel = assignedZone?.name
        ?: animal.zoneName
        ?: animal.assignedPerimeterId?.let { stringResource(R.string.assigned_zone) }
        ?: stringResource(R.string.no_assigned_zone)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScreenHeader(
                        title = animal.name.ifBlank { stringResource(R.string.animal_detail_title) },
                        subtitle = stringResource(R.string.animal_detail_subtitle)
                    )
                }
                if (isLoading) {
                    item { LoadingContent() }
                } else {
                    detailError?.let {
                        item { WarningPanel(it.asString()) }
                        item {
                            BluePatitasOutlinedButton(
                                text = stringResource(R.string.retry),
                                onClick = onRetry
                            )
                        }
                    }
                    item {
                        AnimalPhotoPlaceholder(
                            animal = animal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    item { DetailRow(stringResource(R.string.species), animal.species.ifBlank { stringResource(R.string.not_available) }) }
                    item { DetailRow(stringResource(R.string.breed), animal.breed ?: stringResource(R.string.not_available)) }
                    item {
                        DetailRow(
                            stringResource(R.string.estimated_age_months),
                            animal.estimatedAgeMonths?.toString() ?: stringResource(R.string.not_available)
                        )
                    }
                    item {
                        DetailRow(
                            stringResource(R.string.weight_kg),
                            animal.weightKg?.let { stringResource(R.string.kg_value, it) } ?: stringResource(R.string.not_available)
                        )
                    }
                    item { DetailRow(stringResource(R.string.health_condition), animal.healthCondition.healthConditionLabel()) }
                    if (healthUpdatedMessageVisible) {
                        item {
                            SuccessPanel(
                                text = stringResource(R.string.health_update_success),
                                onDismiss = onDismissHealthUpdatedMessage
                            )
                        }
                    }
                    healthUpdateError?.let {
                        item { WarningPanel(it.asString()) }
                    }
                    item {
                        if (showHealthEditor) {
                            HealthConditionEditor(
                                currentCondition = animal.healthCondition,
                                selectedCondition = selectedHealthCondition,
                                isUpdating = isUpdatingHealth,
                                onSelect = onSelectHealthCondition,
                                onConfirm = onUpdateAnimalHealth,
                                onCancel = onHideHealthEditor
                            )
                        } else {
                            BluePatitasOutlinedButton(
                                text = stringResource(R.string.change_health_condition),
                                onClick = onShowHealthEditor
                            )
                        }
                    }
                    if (zoneAssignedMessageVisible) {
                        item {
                            SuccessPanel(
                                text = stringResource(R.string.animal_assigned_to_zone),
                                onDismiss = onDismissZoneAssignedMessage
                            )
                        }
                    }
                    item {
                        ZoneAssignmentSection(
                            assignedZoneLabel = assignedZoneLabel,
                            hasAssignedZone = animal.assignedPerimeterId != null,
                            zones = zones,
                            selectedPerimeterId = selectedPerimeterId,
                            showEditor = showZoneAssignmentEditor,
                            isAssigning = isAssigningZone,
                            error = zoneAssignmentError,
                            onShowEditor = onShowZoneAssignmentEditor,
                            onHideEditor = onHideZoneAssignmentEditor,
                            onSelectPerimeter = onSelectPerimeter,
                            onConfirm = onAssignAnimalToZone,
                            onRemove = onRemoveAnimalZone
                        )
                    }
                }
                item {
                    BluePatitasOutlinedButton(text = stringResource(R.string.close), onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun ZoneAssignmentSection(
    assignedZoneLabel: String,
    hasAssignedZone: Boolean,
    zones: List<MonitoringZone>,
    selectedPerimeterId: String?,
    showEditor: Boolean,
    isAssigning: Boolean,
    error: AnimalActionError?,
    onShowEditor: () -> Unit,
    onHideEditor: () -> Unit,
    onSelectPerimeter: (String) -> Unit,
    onConfirm: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7FAFD),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE3F0FF))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.assigned_zone),
                style = MaterialTheme.typography.titleSmall,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = assignedZoneLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasAssignedZone) BluePrimary else MutedInk,
                fontWeight = FontWeight.Medium
            )
            error?.let {
                WarningPanel(it.asString())
            }
            if (showEditor) {
                if (zones.isEmpty()) {
                    EmptyPanel(stringResource(R.string.no_zones_available))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        zones.forEach { zone ->
                            ZoneAssignmentOption(
                                zone = zone,
                                selected = selectedPerimeterId == zone.id,
                                onSelect = { onSelectPerimeter(zone.id) }
                            )
                        }
                    }
                }
                if (isAssigning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = BluePrimary,
                        trackColor = Color(0xFFDCEAF8)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BluePatitasOutlinedButton(
                        text = stringResource(R.string.cancel),
                        onClick = onHideEditor,
                        enabled = !isAssigning,
                        modifier = Modifier.weight(1f)
                    )
                    BluePatitasPrimaryButton(
                        text = stringResource(R.string.assign_zone),
                        onClick = onConfirm,
                        enabled = !isAssigning && selectedPerimeterId != null && zones.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (hasAssignedZone) {
                    BluePatitasOutlinedButton(
                        text = stringResource(R.string.remove_zone),
                        onClick = onRemove,
                        enabled = !isAssigning
                    )
                }
            } else {
                BluePatitasOutlinedButton(
                    text = if (hasAssignedZone) stringResource(R.string.change_zone) else stringResource(R.string.assign_zone),
                    onClick = onShowEditor
                )
            }
        }
    }
}

@Composable
private fun ZoneAssignmentOption(
    zone: MonitoringZone,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFEAF4FF) else Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (selected) BluePrimary else Color(0xFFE3F0FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = zone.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = BlueDark,
                    fontWeight = FontWeight.Bold
                )
                StatusPill(
                    text = zone.status.ifBlank { stringResource(R.string.normal) },
                    critical = zone.status.contains("alert", ignoreCase = true)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(
                    label = stringResource(R.string.camera),
                    value = if (zone.cameraEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                    modifier = Modifier.weight(1f)
                )
                SmallMetric(
                    label = stringResource(R.string.animals),
                    value = zone.animalCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HealthConditionEditor(
    currentCondition: String?,
    selectedCondition: String?,
    isUpdating: Boolean,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF6FAFE),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFD8E9FA))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.select_health_condition),
                style = MaterialTheme.typography.titleSmall,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )
            AnimalHealthOption.entries.forEach { option ->
                val selected = selectedCondition == option.apiValue
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isUpdating) { onSelect(option.apiValue) },
                    color = if (selected) Color(0xFFEAF4FF) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selected) BluePrimary else Color(0xFFE3F0FF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = option.label(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueDark,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (currentCondition.normalizeHealthCondition() == option.apiValue) {
                            StatusPill(text = stringResource(R.string.current))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BluePatitasOutlinedButton(
                    text = stringResource(R.string.cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                BluePatitasPrimaryButton(
                    text = stringResource(R.string.confirm_health_update),
                    onClick = onConfirm,
                    enabled = !isUpdating &&
                        selectedCondition != null &&
                        selectedCondition != currentCondition.normalizeHealthCondition(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AnimalDataBanner(
    error: AnimalActionError,
    usingFallback: Boolean,
    onRetry: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (usingFallback) Color(0xFFFFF9E6) else Color(0xFFFFF5F5)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (usingFallback) Color(0xFFFFE0B2) else Color(0xFFFFD1D1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (usingFallback) {
                    stringResource(R.string.animals_demo_data_banner)
                } else {
                    stringResource(R.string.animals_load_error_banner)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (usingFallback) Color(0xFFE65100) else RedCritical,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (usingFallback) Color(0xFFE65100) else RedCritical
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (usingFallback) Color(0xFFFFB74D) else RedCritical)
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    color = if (usingFallback) Color(0xFFE65100) else RedCritical,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnimalImagePicker(
    form: AnimalFormUiState,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF4F9FF),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFD6E8FA))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (form.selectedImageUri.isNullOrBlank()) {
                AnimalPhotoPlaceholder(
                    animal = AnimalSummary("", "", null, form.species, null, null, null, null, null),
                    modifier = Modifier.size(112.dp)
                )
                Text(
                    text = stringResource(R.string.image_placeholder_now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueDark,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            } else {
                AsyncImage(
                    model = form.selectedImageUri,
                    contentDescription = stringResource(R.string.animal_photo_description),
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFD6E8FA), RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = stringResource(R.string.selected_image_ready),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenSuccess,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onPickImage,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BluePrimary)
                ) {
                    Text(
                        text = stringResource(R.string.select_animal_photo),
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!form.selectedImageUri.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onClearImage,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedCritical)
                    ) {
                        Text(
                            text = stringResource(R.string.remove_animal_photo),
                            color = RedCritical,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAnimalPanel(onRegisterFirst: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE9EFF5)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimalPhotoPlaceholder(
                animal = AnimalSummary("", "", null, "", null, null, null, null, null),
                modifier = Modifier.size(82.dp)
            )
            Text(
                text = stringResource(R.string.no_animals_registered_title),
                style = MaterialTheme.typography.titleMedium,
                color = BlueDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.no_animals_registered_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                textAlign = TextAlign.Center
            )
            BluePatitasPrimaryButton(
                text = stringResource(R.string.register_first_animal),
                onClick = onRegisterFirst
            )
        }
    }
}

@Composable
private fun SuccessPanel(text: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEAF8F0),
        border = BorderStroke(1.dp, Color(0xFFC7EFD7)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = GreenSuccess,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GreenSuccess)
            ) {
                Text(
                    text = stringResource(R.string.close),
                    color = GreenSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnimalPhotoPlaceholder(animal: AnimalSummary, modifier: Modifier = Modifier) {
    var imageLoadFailed by remember(animal.photoUrl) { mutableStateOf(false) }
    val photoUrl = animal.photoUrl?.takeIf { it.isNotBlank() && !imageLoadFailed }
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = stringResource(R.string.animal_photo_description),
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFFE3F0FF), RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
            onError = { imageLoadFailed = true }
        )
        return
    }
    val species = animal.species.lowercase()
    val isDog = species.contains("dog") || species.contains("perro") || species.contains("canino")
    val isCat = species.contains("cat") || species.contains("gato") || species.contains("felino")
    val label = when {
        isDog -> stringResource(R.string.placeholder_dog)
        isCat -> stringResource(R.string.placeholder_cat)
        else -> stringResource(R.string.placeholder_pet)
    }
    val backgroundColor = when {
        isDog -> Color(0xFFE8F4FF)
        isCat -> Color(0xFFF2EDFF)
        else -> Color(0xFFEAF8F0)
    }
    val borderColor = when {
        isDog -> Color(0xFFB9DCF9)
        isCat -> Color(0xFFD7C8F7)
        else -> Color(0xFFC7EFD7)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = BluePrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7FAFD),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE3F0FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MutedInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = BlueDark,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun AnimalFieldError.asString(): String =
    when (this) {
        AnimalFieldError.Required -> stringResource(R.string.required_field)
        AnimalFieldError.InvalidAge -> stringResource(R.string.animal_age_error)
        AnimalFieldError.InvalidWeight -> stringResource(R.string.animal_weight_error)
    }

@Composable
private fun AnimalActionError.asString(): String =
    when (this) {
        AnimalActionError.BadRequest -> stringResource(R.string.animal_error_bad_request)
        AnimalActionError.SessionExpired -> stringResource(R.string.auth_error_session_expired)
        AnimalActionError.EndpointNotFound -> stringResource(R.string.auth_error_endpoint_not_found)
        AnimalActionError.ServerError -> stringResource(R.string.auth_error_server)
        AnimalActionError.Timeout -> stringResource(R.string.auth_error_timeout)
        AnimalActionError.Network -> stringResource(R.string.auth_error_network)
        AnimalActionError.ResponseFormat -> stringResource(R.string.auth_error_response_format)
        AnimalActionError.InvalidImageUpload -> stringResource(R.string.animal_image_upload_error)
        AnimalActionError.Unknown -> stringResource(R.string.connection_error)
    }

@Composable
private fun AnimalActionError.asMonitoringString(): String =
    when (this) {
        AnimalActionError.BadRequest -> stringResource(R.string.monitoring_error_bad_request)
        AnimalActionError.SessionExpired -> stringResource(R.string.auth_error_session_expired)
        AnimalActionError.EndpointNotFound -> stringResource(R.string.monitoring_error_not_found)
        AnimalActionError.ServerError -> stringResource(R.string.monitoring_error_server)
        AnimalActionError.Timeout,
        AnimalActionError.Network -> stringResource(R.string.monitoring_error_network)
        AnimalActionError.ResponseFormat -> stringResource(R.string.auth_error_response_format)
        AnimalActionError.InvalidImageUpload -> stringResource(R.string.animal_image_upload_error)
        AnimalActionError.Unknown -> stringResource(R.string.connection_error)
    }

@Composable
private fun MonitoringFieldError.asString(): String =
    when (this) {
        MonitoringFieldError.Required -> stringResource(R.string.required_field)
        MonitoringFieldError.InvalidNumber -> stringResource(R.string.monitoring_number_error)
        MonitoringFieldError.InvalidCount -> stringResource(R.string.monitoring_count_error)
        MonitoringFieldError.InvalidRange -> stringResource(R.string.monitoring_temperature_range_error)
    }

@Composable
private fun CreateZoneDialog(
    form: MonitoringZoneFormUiState,
    errors: Map<String, MonitoringFieldError>,
    isSaving: Boolean,
    actionError: AnimalActionError?,
    onFieldChange: (String, String) -> Unit,
    onCameraEnabledChange: (Boolean) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScreenHeader(
                        title = stringResource(R.string.create_zone),
                        subtitle = stringResource(R.string.create_zone_subtitle)
                    )
                }
                actionError?.let {
                    item { WarningPanel(it.asMonitoringString()) }
                }
                if (isSaving) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = BluePrimary,
                            trackColor = Color(0xFFDCEAF8)
                        )
                    }
                }
                item {
                    BluePatitasTextField(
                        value = form.name,
                        onValueChange = { onFieldChange("name", it) },
                        label = stringResource(R.string.zone_name),
                        error = errors["name"]?.asString()
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BluePatitasTextField(
                            value = form.targetId,
                            onValueChange = { onFieldChange("targetId", it) },
                            label = stringResource(R.string.iot_device_id)
                        )
                        Text(
                            text = stringResource(R.string.iot_device_id_auto_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }
                item {
                    BluePatitasTextField(
                        value = form.status,
                        onValueChange = { onFieldChange("status", it) },
                        label = stringResource(R.string.status),
                        error = errors["status"]?.asString()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BluePatitasTextField(
                            value = form.temperatureC,
                            onValueChange = { onFieldChange("temperatureC", it) },
                            label = stringResource(R.string.temperature_short),
                            error = errors["temperatureC"]?.asString(),
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        BluePatitasTextField(
                            value = form.humidity,
                            onValueChange = { onFieldChange("humidity", it) },
                            label = stringResource(R.string.humidity_short),
                            error = errors["humidity"]?.asString(),
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BluePatitasTextField(
                            value = form.minTemperatureC,
                            onValueChange = { onFieldChange("minTemperatureC", it) },
                            label = stringResource(R.string.min_temp),
                            error = errors["minTemperatureC"]?.asString(),
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        BluePatitasTextField(
                            value = form.maxTemperatureC,
                            onValueChange = { onFieldChange("maxTemperatureC", it) },
                            label = stringResource(R.string.max_temp),
                            error = errors["maxTemperatureC"]?.asString(),
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    BluePatitasTextField(
                        value = form.animalCount,
                        onValueChange = { onFieldChange("animalCount", it) },
                        label = stringResource(R.string.animal_count),
                        error = errors["animalCount"]?.asString(),
                        keyboardType = KeyboardType.Number
                    )
                }
                item {
                    BluePatitasTextField(
                        value = form.imageUrl,
                        onValueChange = { onFieldChange("imageUrl", it) },
                        label = stringResource(R.string.zone_image_optional)
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onCameraEnabledChange(true) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (form.cameraEnabled) BluePrimary else Color(0xFFEAF4FF),
                                contentColor = if (form.cameraEnabled) Color.White else BluePrimary
                            )
                        ) {
                            Text(stringResource(R.string.camera_enabled), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onCameraEnabledChange(false) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (!form.cameraEnabled) BluePrimary else Color(0xFFEAF4FF),
                                contentColor = if (!form.cameraEnabled) Color.White else BluePrimary
                            )
                        ) {
                            Text(stringResource(R.string.camera_disabled), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BluePatitasOutlinedButton(
                            text = stringResource(R.string.cancel),
                            onClick = { if (!isSaving) onDismiss() },
                            modifier = Modifier.weight(1f)
                        )
                        BluePatitasPrimaryButton(
                            text = stringResource(R.string.save_zone),
                            onClick = onCreate,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryPanel(
    telemetry: List<TelemetryRecord>,
    isLoading: Boolean,
    error: AnimalActionError?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF4F9FF),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFD6E8FA))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.telemetry),
                style = MaterialTheme.typography.titleSmall,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = BluePrimary,
                    trackColor = Color(0xFFDCEAF8)
                )
            }
            error?.let {
                Text(
                    text = error.asMonitoringString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = RedCritical
                )
            }
            when {
                telemetry.isEmpty() && !isLoading -> Text(
                    text = stringResource(R.string.no_telemetry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
                telemetry.isNotEmpty() -> telemetry.take(3).forEach { record ->
                    TelemetryRecordItem(record = record)
                }
            }
        }
    }
}

@Composable
private fun IotDeviceIdPanel(targetId: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.iot_device_id_copied)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7FAFD),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE3F0FF))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.iot_device_id),
                style = MaterialTheme.typography.labelMedium,
                color = MutedInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.iot_device_id_usage),
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = targetId.shortTargetLabel(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueDark,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                BluePatitasOutlinedButton(
                    text = stringResource(R.string.copy_id),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(targetId))
                        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun TelemetryRecordItem(record: TelemetryRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0EDF8))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(
                    label = stringResource(R.string.temperature_short),
                    value = record.ambientTemperature.temperatureValue(),
                    modifier = Modifier.weight(1f)
                )
                SmallMetric(
                    label = stringResource(R.string.humidity_short),
                    value = record.ambientHumidity.percentValue(),
                    modifier = Modifier.weight(1f)
                )
            }
            record.recordedAt?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.recorded_at_value, it.friendlyRecordedAt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
            record.visualData?.takeIf { it.isNotBlank() }?.let {
                if (it.startsWith("http", ignoreCase = true)) {
                    AsyncImage(
                        model = it,
                        contentDescription = stringResource(R.string.telemetry_visual_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }
        }
    }
}

@Composable
private fun MonitoringZoneCard(zone: MonitoringZone, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFF2F8FF) else Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 1.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) BluePrimary else Color(0xFFE9EFF5)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlueDark)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (zone.cameraEnabled) GreenSuccess else Color.Gray)
                        )
                        Text(
                            text = stringResource(R.string.zone_camera_status, if (zone.cameraEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
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
    telemetry: List<TelemetryRecord>,
    isLoadingTelemetry: Boolean,
    telemetryError: AnimalActionError?,
    onActivateCamera: () -> Unit,
    onStopCamera: () -> Unit,
    onSimulateBreach: () -> Unit,
    onSimulateSafe: () -> Unit
) {
    val isBreach = geofenceStatus == GeofenceStatus.OutsideSafeZone
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BlueDark)
                    Text(
                        text = stringResource(R.string.zone_detail_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
                StatusPill(
                    text = if (isBreach) stringResource(R.string.outside_safe_zone) else stringResource(R.string.inside_safe_zone),
                    critical = isBreach
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (phoneCameraActive) {
                    PhoneCameraPreview()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.phone_camera_live_preview),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    CameraPlaceholder(imageUrl = zone.imageUrl)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(stringResource(R.string.min_temp), "${zone.minTemperatureC?.toInt() ?: 0}°C", Modifier.weight(1f))
                SmallMetric(stringResource(R.string.max_temp), "${zone.maxTemperatureC?.toInt() ?: 0}°C", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric(stringResource(R.string.temperature_short), zone.temperatureC.temperatureValue(), Modifier.weight(1f))
                SmallMetric(stringResource(R.string.humidity_short), zone.humidity.percentValue(), Modifier.weight(1f))
                SmallMetric(stringResource(R.string.animals), zone.animalCount.toString(), Modifier.weight(1f))
            }
            TelemetryPanel(
                telemetry = telemetry,
                isLoading = isLoadingTelemetry,
                error = telemetryError
            )
            if (zone.targetId.isNotBlank()) {
                IotDeviceIdPanel(targetId = zone.targetId)
            }
            if (isBreach) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFC1C1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            color = RedCritical,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.pet_left_safe_zone),
                                color = RedCritical,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.local_simulation_label),
                                color = Color(0xFF8C3E3E),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFF4F9FF),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.current_status).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = stringResource(R.string.zone_environment_value, zone.temperatureC?.toInt() ?: 0, zone.humidity?.toInt() ?: 0),
                                style = MaterialTheme.typography.bodySmall,
                                color = BlueDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GreenSuccess))
                            Text(
                                text = stringResource(R.string.tracking_active),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedInk
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (phoneCameraActive) {
                    OutlinedButton(
                        onClick = onStopCamera,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BluePrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.stop_camera),
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onActivateCamera,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.activate_phone_camera),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSimulateSafe,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFB0BEC5))
                ) {
                    Text(
                        text = stringResource(R.string.simulate_safe_position),
                        color = Color(0xFF37474F),
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onSimulateBreach,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1), contentColor = Color(0xFF37474F))
                ) {
                    Text(
                        text = stringResource(R.string.simulate_breach),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: MonitoringAlert,
    resolving: Boolean,
    enablingTracking: Boolean,
    onResolve: (String) -> Unit,
    onEnableTracking: (MonitoringAlert) -> Unit
) {
    val isCritical = alert.isBreachConfirmed
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isCritical) Color(0xFFFFF5F5) else Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCritical) Color(0xFFFFD1D1) else Color(0xFFE9EFF5)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.perimeter_alert_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlueDark
                    )
                    Text(
                        text = alert.zoneName.ifBlank { alert.targetId.shortTargetLabel() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCritical) Color(0xFF8C3E3E) else Ink
                    )
                }
                StatusPill(
                    text = if (alert.isBreachConfirmed) stringResource(R.string.alert_confirmed) else stringResource(R.string.alert_not_confirmed),
                    critical = isCritical
                )
            }
            Text(
                text = alert.message.ifBlank { stringResource(R.string.perimeter_alert_message) },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCritical) Color(0xFF8C3E3E) else MutedInk
            )
            if (alert.latitude != null && alert.longitude != null) {
                Surface(
                    color = if (isCritical) Color(0xFFFFECEC) else Color(0xFFF5F9FD),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 " + stringResource(R.string.coordinates_value, alert.latitude, alert.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCritical) Color(0xFF8C3E3E) else MutedInk,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        StatusPill(
                            text = if (alert.trackingActive) stringResource(R.string.tracking_active) else stringResource(R.string.tracking_inactive),
                            critical = false
                        )
                    }
                }
            }
            if (alert.targetId.isNotBlank()) {
                DetailRow(stringResource(R.string.iot_device_id), alert.targetId.shortTargetLabel())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BluePatitasOutlinedButton(
                    text = if (resolving) stringResource(R.string.saving) else stringResource(R.string.resolve_alert),
                    onClick = { onResolve(alert.id) },
                    modifier = Modifier.weight(1f)
                )
                if (!alert.trackingActive && alert.targetId.isNotBlank() && !alert.isLocal) {
                    BluePatitasPrimaryButton(
                        text = if (enablingTracking) stringResource(R.string.saving) else stringResource(R.string.enable_tracking),
                        onClick = { onEnableTracking(alert) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPlaceholder(imageUrl: String?) {
    var imageLoadFailed by remember(imageUrl) { mutableStateOf(false) }
    val remoteImage = imageUrl?.takeIf { it.isNotBlank() && !imageLoadFailed }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F2FA)),
        contentAlignment = Alignment.Center
    ) {
        if (remoteImage != null) {
            AsyncImage(
                model = remoteImage,
                contentDescription = stringResource(R.string.zone_image_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { imageLoadFailed = true }
            )
            return@Box
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📷",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.camera_snapshot_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.simulated_for_presentation),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WarningPanel(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF5F5),
        border = BorderStroke(1.dp, Color(0xFFFFD1D1)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", color = RedCritical)
            Text(
                text = text,
                color = RedCritical,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyPanel(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE9EFF5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F9FD),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE3F0FF))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MutedInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = BlueDark,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, critical: Boolean = false) {
    val color = when {
        critical || text.contains("critical", true) || text.contains("alert", true) || text.contains("outside", true) -> RedCritical
        text.contains("warning", true) || text.contains("observation", true) -> AmberWarning
        else -> GreenSuccess
    }
    val bgAlpha = 0.12f
    val textLabel = text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    Surface(
        color = color.copy(alpha = bgAlpha),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
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

@Composable
private fun ZoneCompactCard(zone: MonitoringZone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE9EFF5))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlueDark)
                Text(
                    text = stringResource(R.string.zone_environment_value, zone.temperatureC?.toInt() ?: 0, zone.humidity?.toInt() ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
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
            .background(Color(0xFFEAF4FF))
            .border(2.dp, Color(0xFFD0E3FA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text.initials(), color = BluePrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = BlueDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun roleLabel(role: UserRole): String =
    when (role) {
        UserRole.SHELTER_ADMIN -> stringResource(R.string.role_shelter_admin)
        UserRole.VETERINARIAN -> stringResource(R.string.role_veterinarian)
    }

@Composable
private fun String?.healthConditionLabel(): String =
    when (AnimalHealthOption.fromApiValue(this)) {
        AnimalHealthOption.Healthy -> stringResource(R.string.health_condition_healthy)
        AnimalHealthOption.InTreatment -> stringResource(R.string.health_condition_in_treatment)
        AnimalHealthOption.Critical -> stringResource(R.string.health_condition_critical)
        AnimalHealthOption.UnderObservation -> stringResource(R.string.health_condition_under_observation)
        null -> this?.takeIf { it.isNotBlank() } ?: stringResource(R.string.not_available)
    }

@Composable
private fun AnimalHealthOption.label(): String = apiValue.healthConditionLabel()

private fun String?.normalizeHealthCondition(): String? =
    AnimalHealthOption.fromApiValue(this)?.apiValue

private fun Double?.temperatureValue(): String =
    this?.let { "${it.toInt()}°C" } ?: "-"

private fun Double?.percentValue(): String =
    this?.let { "${it.toInt()}%" } ?: "-"

private fun String.shortTargetLabel(): String =
    ifBlank { "" }.let { value ->
        if (value.length > 10) "${value.take(8)}..." else value
    }

private fun String.isLongTechnicalId(): Boolean =
    length > 18 || count { it == '-' } >= 3

private fun String.friendlyRecordedAt(): String =
    runCatching {
        OffsetDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    }.getOrDefault(this)

private fun String.initials(): String =
    trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "BP" }

private fun String.firstName(): String = trim().substringBefore(" ").ifBlank { this }
