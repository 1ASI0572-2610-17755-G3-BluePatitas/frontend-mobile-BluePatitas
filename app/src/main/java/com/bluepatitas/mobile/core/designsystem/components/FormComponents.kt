package com.bluepatitas.mobile.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.designsystem.icons.PawLogoIcon
import com.bluepatitas.mobile.core.designsystem.theme.BlueDark
import com.bluepatitas.mobile.core.designsystem.theme.BluePrimary
import com.bluepatitas.mobile.core.designsystem.theme.BlueSurface
import com.bluepatitas.mobile.core.designsystem.theme.GreenSuccess
import com.bluepatitas.mobile.core.designsystem.theme.Spacing
import com.bluepatitas.mobile.core.designsystem.theme.White

@Composable
fun BluePatitasTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        isError = error != null,
        supportingText = error?.let { { Text(text = it) } },
        singleLine = singleLine,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = Color(0xFFD6E3F3),
            focusedContainerColor = White,
            unfocusedContainerColor = Color(0xFFF5F8FC),
            errorContainerColor = Color(0xFFFFF5F5),
            focusedLabelColor = BluePrimary,
            unfocusedLabelColor = Color(0xFF5C6B7A)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BluePatitasPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val visibilityDescription = if (visible) {
        stringResource(R.string.hide_password)
    } else {
        stringResource(R.string.show_password)
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        isError = error != null,
        supportingText = error?.let { { Text(text = it) } },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = Color(0xFFD6E3F3),
            focusedContainerColor = White,
            unfocusedContainerColor = Color(0xFFF5F8FC),
            errorContainerColor = Color(0xFFFFF5F5),
            focusedLabelColor = BluePrimary,
            unfocusedLabelColor = Color(0xFF5C6B7A)
        ),
        trailingIcon = {
            IconButton(
                onClick = { onVisibilityChange(!visible) },
                modifier = Modifier.semantics {
                    contentDescription = visibilityDescription
                }
            ) {
                Text(
                    text = if (visible) {
                        stringResource(R.string.hide_password_short)
                    } else {
                        stringResource(R.string.show_password_short)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = BluePrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BluePatitasCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = BlueDark)
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun BluePatitasStepIndicator(
    currentStep: Int,
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, label ->
            StepPill(
                label = label,
                active = index == currentStep,
                completed = index < currentStep
            )
        }
    }
}

@Composable
private fun RowScope.StepPill(
    label: String,
    active: Boolean,
    completed: Boolean
) {
    val background = when {
        active -> MaterialTheme.colorScheme.primary
        completed -> GreenSuccess
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (active || completed) White else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = label,
        color = content,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)
            .background(background, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
fun BluePatitasAuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val logoDescription = stringResource(R.string.bluepatitas_logo_description)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White, CircleShape)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            PawLogoIcon(
                color = BluePrimary,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = logoDescription
                    }
            )
        }
        Text(
            text = title,
            color = BlueDark,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF5C6B7A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BluePatitasInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = BlueDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            if (supportingText != null) {
                Text(text = supportingText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5C6B7A))
            }
        }
    }
}

@Composable
fun BluePatitasFormScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    topContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F7FD))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        BluePatitasAuthHeader(title = title, subtitle = subtitle)
        topContent?.invoke()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
        bottomContent?.invoke()
        Spacer(modifier = Modifier.height(16.dp))
    }
}
