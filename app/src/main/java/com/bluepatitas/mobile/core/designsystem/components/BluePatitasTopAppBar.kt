package com.bluepatitas.mobile.core.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluePatitasTopAppBar(
    title: String
) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BlueDark,
            titleContentColor = White
        )
    )
}
