package com.firestormsw.listflow.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.CornerRadius
import com.firestormsw.listflow.ui.theme.PanelInactive
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary

@Composable
fun StyledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = Accent,
    content: @Composable (RowScope.() -> Unit),
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = ButtonColors(
            containerColor = accentColor,
            contentColor = TextPrimary,
            disabledContainerColor = PanelInactive,
            disabledContentColor = TextSecondary
        )
    ) {
        content()
    }
}