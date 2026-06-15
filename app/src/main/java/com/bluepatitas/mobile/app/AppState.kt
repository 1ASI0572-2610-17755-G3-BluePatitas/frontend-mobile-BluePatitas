package com.bluepatitas.mobile.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Stable
class AppState(
    val navController: NavHostController
)

@Composable
fun rememberBluePatitasAppState(
    navController: NavHostController = rememberNavController()
): AppState = remember(navController) {
    AppState(navController)
}
