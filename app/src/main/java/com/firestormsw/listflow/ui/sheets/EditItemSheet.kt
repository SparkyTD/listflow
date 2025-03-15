package com.firestormsw.listflow.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.R
import com.firestormsw.listflow.data.model.ListItemModel
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.ui.components.SheetDragHandle
import com.firestormsw.listflow.ui.components.StyledButton
import com.firestormsw.listflow.ui.components.StyledTextField
import com.firestormsw.listflow.ui.icons.Add
import com.firestormsw.listflow.ui.icons.Remove
import com.firestormsw.listflow.ui.theme.Background
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.Typography
import ulid.ULID
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    isOpen: Boolean,
    list: ListModel?,
    onDismiss: () -> Unit,
    onSave: (ListItemModel) -> Unit,
    editListItem: ListItemModel? = null
) {
    if (!isOpen || list == null) {
        return
    }

    var itemTextInput by remember(editListItem) { mutableStateOf(editListItem?.text ?: "") }
    var itemUnitInput by remember { mutableStateOf(editListItem?.unit ?: "") }
    var itemQuantityInput by remember { mutableStateOf(editListItem?.quantity?.toString() ?: "") }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { SheetDragHandle() },
        containerColor = Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // List item text input field
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)) {
                StyledTextField(
                    value = itemTextInput,
                    onValueChange = { text -> itemTextInput = text },
                    label = stringResource(R.string.item_name),
                    singleLine = true,
                    initialCursorAtEnd = true,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            // Unit and quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val parsedFloat = itemQuantityInput.toFloatOrNull()
                StyledTextField(
                    value = if (parsedFloat == null) {
                        ""
                    } else if (itemQuantityInput.endsWith('.') || itemQuantityInput.endsWith(',')) {
                        itemQuantityInput
                    } else if (parsedFloat.rem(1).equals(0.0f)) {
                        parsedFloat.toInt().toString()
                    } else {
                        parsedFloat.toString()
                    },
                    onValueChange = {
                        itemQuantityInput = it
                    },
                    label = stringResource(R.string.quantity),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.3333f)
                        .height(64.dp)
                )
                StyledTextField(
                    value = itemUnitInput,
                    onValueChange = { itemUnitInput = it },
                    label = stringResource(R.string.unit),
                    singleLine = true,
                    modifier = Modifier
                        .weight(0.3333f)
                        .height(64.dp)
                )

                // Decrease quantity
                StyledButton(
                    onClick = {
                        if (parsedFloat == null) {
                            itemQuantityInput = "1"
                        } else if (parsedFloat > 1) {
                            itemQuantityInput = (parsedFloat - 1).toString()
                        }
                    },
                    accentColor = PanelActive,
                    modifier = Modifier
                        .weight(0.1666f)
                        .height(60.dp)
                        .padding(top = 4.dp)
                ) {
                    Icon(Remove, contentDescription = null, modifier = Modifier.scale(4f))
                }

                // Increase quantity
                StyledButton(
                    onClick = {
                        itemQuantityInput = if (parsedFloat == null) {
                            "1"
                        } else {
                            (parsedFloat + 1).toString()
                        }
                    },
                    accentColor = PanelActive,
                    modifier = Modifier
                        .weight(0.1666f)
                        .height(60.dp)
                        .padding(top = 4.dp)
                ) {
                    Icon(Add, contentDescription = null, modifier = Modifier.scale(4f))
                }
            }

            // Save button
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)) {
                StyledButton(
                    onClick = {
                        onSave(
                            ListItemModel(
                                id = editListItem?.id ?: ULID.randomULID(),
                                text = itemTextInput.trim(),
                                unit = itemUnitInput.trim(),
                                quantity = itemQuantityInput.toFloatOrNull(),
                                isHighlighted = false,
                                isChecked = false,
                                createdAt = Date.from(Instant.now()),
                                listId = list.id
                            )
                        )
                    },
                    enabled = itemTextInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (editListItem == null) {
                            stringResource(R.string.create_item)
                        } else {
                            stringResource(R.string.save_item)
                        },
                        style = Typography.bodyLarge
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}