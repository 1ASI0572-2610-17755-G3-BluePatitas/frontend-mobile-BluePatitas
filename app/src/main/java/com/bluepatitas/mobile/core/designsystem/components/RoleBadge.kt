package com.bluepatitas.mobile.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.domain.model.UserRole

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val label = when (role) {
        UserRole.SHELTER_ADMIN -> stringResource(R.string.role_shelter_admin)
        UserRole.VETERINARIAN -> stringResource(R.string.role_veterinarian)
    }
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = {
            Row(modifier = Modifier.padding(horizontal = 2.dp)) {
                Text(text = label)
            }
        }
    )
}
