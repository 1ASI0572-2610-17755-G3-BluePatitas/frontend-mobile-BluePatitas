package com.bluepatitas.mobile.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasOutlinedButton
import com.bluepatitas.mobile.core.designsystem.components.BluePatitasTopAppBar
import com.bluepatitas.mobile.core.designsystem.components.RoleBadge
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.feature.main.AdminAnimalsRoute
import com.bluepatitas.mobile.feature.main.AdminHomeRoute
import com.bluepatitas.mobile.feature.main.AlertsRoute
import com.bluepatitas.mobile.feature.main.MainDataViewModel
import com.bluepatitas.mobile.feature.main.MonitoringRoute
import com.bluepatitas.mobile.feature.main.ProfileRoute
import com.bluepatitas.mobile.feature.veterinary.VeterinaryAnimalsRoute
import com.bluepatitas.mobile.feature.veterinary.VeterinaryDashboardRoute

@Composable
fun RoleNavigationScaffold(
    session: AppSession,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    key(session.role) {
        val navController = rememberNavController()
        val destinations = destinationsFor(session.role)
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val adminMainDataViewModel: MainDataViewModel? = if (session.role == UserRole.SHELTER_ADMIN) {
            hiltViewModel()
        } else {
            null
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                BluePatitasTopAppBar(title = stringResource(R.string.product_name))
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFF5F9FD),
                    tonalElevation = 8.dp
                ) {
                    destinations.forEach { destination ->
                        val selected = currentRoute == destination.route ||
                            (currentRoute == null && destination == destinations.first())
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val iconVector = when (destination.route) {
                                    "admin_home", "vet_home" -> Icons.Default.Home
                                    "admin_animals", "vet_animals" -> Icons.Default.List
                                    "admin_monitoring", "vet_monitoring" -> Icons.Default.Search
                                    "admin_alerts", "vet_alerts" -> Icons.Default.Notifications
                                    "admin_profile", "vet_profile" -> Icons.Default.Person
                                    else -> Icons.Default.Warning
                                }
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = stringResource(destination.titleRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.titleRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1769E0),
                                unselectedIconColor = Color(0xFF5C6B7A),
                                selectedTextColor = Color(0xFF1769E0),
                                unselectedTextColor = Color(0xFF5C6B7A),
                                indicatorColor = Color(0xFFEAF4FF)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = destinations.first().route,
                modifier = Modifier.padding(paddingValues)
            ) {
                destinations.forEach { destination ->
                    composable(destination.route) {
                        when {
                            session.role == UserRole.SHELTER_ADMIN && destination.route == "admin_home" -> {
                                AdminHomeRoute(
                                    session = session,
                                    onOpenAnimals = { navController.navigate("admin_animals") },
                                    onOpenMonitoring = { navController.navigate("admin_monitoring") },
                                    onOpenAlerts = { navController.navigate("admin_alerts") },
                                    viewModel = requireNotNull(adminMainDataViewModel)
                                )
                            }

                            session.role == UserRole.SHELTER_ADMIN && destination.route == "admin_animals" -> {
                                AdminAnimalsRoute(viewModel = requireNotNull(adminMainDataViewModel))
                            }

                            session.role == UserRole.SHELTER_ADMIN && destination.route == "admin_monitoring" -> {
                                MonitoringRoute(session = session, viewModel = requireNotNull(adminMainDataViewModel))
                            }

                            session.role == UserRole.SHELTER_ADMIN && destination.route == "admin_alerts" -> {
                                AlertsRoute(session = session, viewModel = requireNotNull(adminMainDataViewModel))
                            }

                            session.role == UserRole.SHELTER_ADMIN && destination.route == "admin_profile" -> {
                                ProfileRoute(session = session, onSignOut = onSignOut)
                            }

                            session.role == UserRole.VETERINARIAN && destination.route == "vet_home" -> {
                                VeterinaryDashboardRoute(
                                    session = session,
                                    onSignOut = onSignOut
                                )
                            }

                            session.role == UserRole.VETERINARIAN && destination.route == "vet_animals" -> {
                                VeterinaryAnimalsRoute(onSignOut = onSignOut)
                            }

                            session.role == UserRole.VETERINARIAN && destination.route == "vet_monitoring" -> {
                                MonitoringRoute(session = session)
                            }

                            session.role == UserRole.VETERINARIAN && destination.route == "vet_alerts" -> {
                                AlertsRoute(session = session)
                            }

                            session.role == UserRole.VETERINARIAN && destination.route == "vet_profile" -> {
                                ProfileRoute(session = session, onSignOut = onSignOut)
                            }

                            else -> {
                                PlaceholderDestinationScreen(
                                    title = stringResource(destination.titleRes),
                                    description = stringResource(destination.descriptionRes),
                                    session = session,
                                    onSignOut = onSignOut
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderDestinationScreen(
    title: String,
    description: String,
    session: AppSession,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(24.dp)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.current_role),
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )
        RoleBadge(role = session.role)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start
        )
        BluePatitasOutlinedButton(
            text = stringResource(R.string.sign_out),
            onClick = onSignOut
        )
    }
}
