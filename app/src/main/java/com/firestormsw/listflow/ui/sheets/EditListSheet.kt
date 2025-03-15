package com.firestormsw.listflow.ui.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.R
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.ui.components.SheetDragHandle
import com.firestormsw.listflow.ui.components.StyledButton
import com.firestormsw.listflow.ui.components.StyledTextField
import com.firestormsw.listflow.ui.theme.Background
import com.firestormsw.listflow.ui.theme.Typography
import ulid.ULID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (ListModel) -> Unit,
    editList: ListModel? = null
) {
    if (!isOpen) {
        return
    }

    var listNameInput by remember(editList) { mutableStateOf(editList?.name ?: "") }
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
            // List name input field
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)) {
                StyledTextField(
                    value = listNameInput,
                    onValueChange = { text -> listNameInput = text },
                    label = stringResource(R.string.list_name),
                    singleLine = true,
                    initialCursorAtEnd = true,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            // Save button
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)) {
                StyledButton(
                    onClick = {
                        onSave(
                            ListModel(
                                id = editList?.id ?: ULID.randomULID(),
                                name = listNameInput.trim(),
                                isCheckedExpanded = true,
                                items = emptyList()
                            )
                        )
                    },
                    enabled = listNameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (editList == null) {
                            stringResource(R.string.create_list)
                        } else {
                            stringResource(R.string.save_list)
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