package com.bluepatitas.mobile.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluepatitas.mobile.core.designsystem.theme.AmberWarning
import com.bluepatitas.mobile.core.designsystem.theme.BluePatitasTheme
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.core.designsystem.theme.RedCritical
import com.bluepatitas.mobile.core.designsystem.theme.White

enum class BluePatitasStatus {
    Ok,
    Warning,
    Critical
}

@Composable
fun BluePatitasStatusChip(
    text: String,
    status: BluePatitasStatus,
    modifier: Modifier = Modifier
) {
    val background = when (status) {
        BluePatitasStatus.Ok -> GreenSuccess
        BluePatitasStatus.Warning -> AmberWarning
        BluePatitasStatus.Critical -> RedCritical
    }
    val content = if (status == BluePatitasStatus.Warning) Color.Black else White

    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
private fun StatusPreview() {
    BluePatitasTheme {
        BluePatitasStatusChip(text = "OK", status = BluePatitasStatus.Ok)
    }
}
