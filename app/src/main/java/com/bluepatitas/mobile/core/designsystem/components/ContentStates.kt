package com.bluepatitas.mobile.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.bluepatitas.mobile.core.designsystem.theme.BluePatitasTheme

@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(text = stringResource(R.string.loading))
    }
}

@Composable
fun EmptyContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    ContentMessage(title = title, message = message, modifier = modifier)
}

@Composable
fun ErrorContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    ContentMessage(title = title, message = message, modifier = modifier)
}

@Composable
private fun ContentMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    BluePatitasTheme {
        EmptyContent(title = "No data yet", message = "Prepared for later.")
    }
}
