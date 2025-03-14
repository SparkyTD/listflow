package com.firestormsw.listflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.viewmodel.SimpleListViewModel
import com.firestormsw.listflow.ui.icons.Add
import com.firestormsw.listflow.ui.icons.Delete
import com.firestormsw.listflow.ui.icons.Edit
import com.firestormsw.listflow.ui.icons.QrCode
import com.firestormsw.listflow.ui.icons.Share

@Composable
fun ListSelector(
    viewModel: SimpleListViewModel,
    selectedList: ListModel?,
    onListSelected: (ListModel) -> Unit,
    onPromptEditList: (ListModel) -> Unit,
    onPromptShareList: (ListModel) -> Unit,
    onPromptDeleteList: (ListModel) -> Unit,
    onPromptCreateList: () -> Unit,
    onPromptScanCode: () -> Unit,
) {
    var addDropdownExpanded by remember { mutableStateOf(false) }
    var editDropdownExpanded by remember { mutableStateOf(false) }
    var targetList by remember { mutableStateOf<ListModel?>(null) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val lists = viewModel.lists.observeAsState().value ?: emptyList()

    // Main list selector chips
    LazyRow(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(lists) { listModel ->
            var horizontalPosition by remember { mutableStateOf(0.dp) }
            ListSelectorChip(
                selected = listModel.id == selectedList?.id,
                text = listModel.name,
                onClick = { onListSelected(listModel) },
                onLongClick = {
                    editDropdownExpanded = !editDropdownExpanded
                    if (editDropdownExpanded) {
                        targetList = listModel
                        menuOffset = DpOffset(
                            horizontalPosition.div(density.density),
                            0.dp
                        )
                    }
                },
                modifier = Modifier.onGloballyPositioned { pos -> horizontalPosition = pos.positionInRoot().x.dp },
            )
        }
        item {
            var horizontalPosition by remember { mutableStateOf(0.dp) }
            ListSelectorChip(
                selected = false,
                text = "Add",
                leadingIcon = { Icon(Add, contentDescription = null) },
                onLongClick = {
                    addDropdownExpanded = !addDropdownExpanded
                    targetList = null
                    if (addDropdownExpanded) {
                        menuOffset = DpOffset(
                            horizontalPosition.div(density.density),
                            0.dp
                        )
                    }
                },
                onClick = {
                    targetList = null
                    onPromptCreateList()
                },
                modifier = Modifier.onGloballyPositioned { pos -> horizontalPosition = pos.positionInRoot().x.dp },
            )
        }
    }

    // "Add" chip dropdown
    DropdownMenu(
        expanded = addDropdownExpanded,
        onDismissRequest = { addDropdownExpanded = false },
        offset = menuOffset,
    ) {
        DropdownMenuItem(
            text = { Text("Create new list") },
            leadingIcon = { Icon(Add, contentDescription = null) },
            onClick = {
                addDropdownExpanded = false
                onPromptCreateList()
            }
        )
        DropdownMenuItem(
            text = { Text("Scan share code") },
            leadingIcon = { Icon(QrCode, contentDescription = null) },
            onClick = {
                addDropdownExpanded = false
                onPromptScanCode()
            }
        )
    }

    // Edit list dropdown
    DropdownMenu(
        expanded = editDropdownExpanded,
        onDismissRequest = { editDropdownExpanded = false },
        offset = menuOffset,
    ) {
        DropdownMenuItem(
            text = { Text("Edit list") },
            leadingIcon = { Icon(Edit, contentDescription = null) },
            onClick = {
                editDropdownExpanded = false
                if (targetList != null) {
                    onPromptEditList(targetList!!)
                }
            }
        )
        DropdownMenuItem(
            text = { Text("Share list") },
            leadingIcon = { Icon(Share, contentDescription = null) },
            onClick = {
                editDropdownExpanded = false
                if (targetList != null) {
                    onPromptShareList(targetList!!)
                }
            }
        )
        DropdownMenuItem(
            text = { Text("Delete list") },
            leadingIcon = { Icon(Delete, contentDescription = null) },
            onClick = {
                editDropdownExpanded = false
                if (targetList != null) {
                    onPromptDeleteList(targetList!!)
                }
            }
        )
    }
}