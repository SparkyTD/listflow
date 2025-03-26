package com.firestormsw.listflow.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.R
import com.firestormsw.listflow.data.model.ListItemModel
import com.firestormsw.listflow.ui.icons.BookmarkAdd
import com.firestormsw.listflow.ui.icons.BookmarkRemove
import com.firestormsw.listflow.ui.icons.Delete
import com.firestormsw.listflow.ui.icons.Edit
import com.firestormsw.listflow.ui.theme.BorderThickness
import com.firestormsw.listflow.ui.theme.CornerRadius
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.PanelInactive
import com.firestormsw.listflow.ui.theme.TextDanger
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary
import com.firestormsw.listflow.ui.theme.Typography
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoListItem(
    item: ListItemModel,
    onToggleChecked: (ListItemModel) -> Unit,
    onPromptEditItem: (ListItemModel) -> Unit,
    onPromptDeleteItem: (ListItemModel) -> Unit,
    onToggleHighlighted: (ListItemModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var editDropdownExpanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    var horizontalPosition by remember { mutableStateOf(0.dp) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        OutlinedCard(
            border = BorderStroke(if (item.isChecked) BorderThickness.Thick else 0.dp, PanelActive),
            shape = RoundedCornerShape(CornerRadius.Small),
            colors = CardDefaults.outlinedCardColors(containerColor = if (item.isChecked) Color.Transparent else PanelActive),
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {
                        onToggleChecked(item)
                        Log.w("SimpleList", "onToggleChecked()")
                    },
                    onLongClick = {
                        editDropdownExpanded = !editDropdownExpanded
                        if (editDropdownExpanded) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuOffset = DpOffset(
                                horizontalPosition.div(density.density),
                                0.dp
                            )
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                )
                .onGloballyPositioned { pos -> horizontalPosition = pos.positionInRoot().x.dp }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item checkbox
                AnimatedCheckbox(item.isChecked) {
                    onToggleChecked(item)
                }

                // Main content
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Main item text
                    Text(
                        text = item.text,
                        color = if (item.isChecked) TextSecondary else TextPrimary,
                        style = Typography.bodyLarge,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                    )

                    // Quantity and unit
                    if (item.quantity != null && item.quantity != 0f || !item.unit.isNullOrEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.Small),
                            color = if (item.isChecked) PanelActive else TextSecondary
                        ) {
                            Text(
                                text = buildString {
                                    item.quantity?.let {
                                        if (it.rem(1).equals(0.0f)) {
                                            append(it.roundToInt())
                                        } else {
                                            append(it)
                                        }
                                    }
                                    if (item.quantity != null && !item.unit.isNullOrEmpty()) {
                                        append(" ")
                                    }
                                    append(item.unit)
                                },
                                style = Typography.bodyLarge,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Highlighted state
                    if (item.isHighlighted) {
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TextPrimary,
                        )
                    }
                }
            }
        }

        // Edit list dropdown
        DropdownMenu(
            expanded = editDropdownExpanded,
            onDismissRequest = { editDropdownExpanded = false },
            offset = menuOffset,
            containerColor = PanelInactive,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit_item), style = Typography.bodyLarge) },
                leadingIcon = { Icon(Edit, contentDescription = null) },
                onClick = {
                    editDropdownExpanded = false
                    onPromptEditItem(item)
                }
            )
            if (!item.isHighlighted) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.highlight), style = Typography.bodyLarge) },
                    leadingIcon = { Icon(BookmarkAdd, contentDescription = null) },
                    onClick = {
                        editDropdownExpanded = false
                        onToggleHighlighted(item)
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.unhighlight), style = Typography.bodyLarge) },
                    leadingIcon = { Icon(BookmarkRemove, contentDescription = null) },
                    onClick = {
                        editDropdownExpanded = false
                        onToggleHighlighted(item)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_item), style = Typography.bodyLarge, color = TextDanger) },
                leadingIcon = { Icon(Delete, contentDescription = null, tint = TextDanger) },
                onClick = {
                    editDropdownExpanded = false
                    onPromptDeleteItem(item)
                }
            )
        }
    }
}