package com.firestormsw.listflow.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.Typography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListSelectorChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val chipInteractionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    val chipColors: SelectableChipColors = FilterChipDefaults
        .filterChipColors().copy(
            containerColor = PanelActive,
            labelColor = TextPrimary,
            selectedContainerColor = Accent,
            selectedLabelColor = TextPrimary,
            leadingIconColor = Accent
        )

    Box {
        FilterChip(
            selected = selected,
            label = { Text(text, style = Typography.bodyLarge) },
            leadingIcon = leadingIcon,
            onClick = {},
            colors = chipColors,
            border = null,
        )

        Box(
            modifier = modifier
                .matchParentSize()
                .combinedClickable(
                    onLongClick = {
                        if (onLongClick != null) {
                            onLongClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onClick = onClick ?: {},
                    interactionSource = chipInteractionSource,
                    indication = null,
                )
        )
    }
}