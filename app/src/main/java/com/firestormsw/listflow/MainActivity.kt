package com.firestormsw.listflow

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.data.viewmodel.ListflowViewModel
import com.firestormsw.listflow.ui.components.ListSelector
import com.firestormsw.listflow.ui.components.TodoList
import com.firestormsw.listflow.ui.icons.Add
import com.firestormsw.listflow.ui.sheets.EditItemSheet
import com.firestormsw.listflow.ui.sheets.EditListSheet
import com.firestormsw.listflow.ui.sheets.ScanShareCodeSheet
import com.firestormsw.listflow.ui.sheets.ShareListSheet
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.ListflowTheme
import com.firestormsw.listflow.ui.theme.TextPrimary
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ListflowViewModel by viewModels()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ListflowTheme {
                val state by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(state.snackbarMessage) {
                    if (state.snackbarMessage != null) {
                        val result = snackbarHostState.showSnackbar(
                            message = state.snackbarMessage!!,
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                state.snackbarAction?.invoke()
                            }

                            SnackbarResult.Dismissed -> {
                                viewModel.clearSnackbar()
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Box(modifier = Modifier.statusBarsPadding()) {
                            ListSelector(
                                viewModel = viewModel,
                                selectedList = state.selectedList,
                                onListSelected = viewModel::setSelectedList,
                                onPromptCreateList = { viewModel.openEditListSheet(null) },
                                onPromptEditList = viewModel::openEditListSheet,
                                onPromptDeleteList = viewModel::deleteList,
                                onPromptShareList = viewModel::openShareListSheet,
                                onPromptScanCode = viewModel::openScanShareCodeSheet,
                            )
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) { data ->
                            Snackbar(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                action = {
                                    TextButton(
                                        onClick = data::performAction,
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.inversePrimary
                                        )
                                    ) {
                                        Text(data.visuals.actionLabel ?: "")
                                    }
                                }
                            ) {
                                Text(data.visuals.message)
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { viewModel.openEditListItemSheet(null, state.selectedList) },
                            containerColor = Accent,
                            contentColor = TextPrimary,
                            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 62.dp),
                        ) {
                            Icon(Add, contentDescription = null)
                        }
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        TodoList(
                            list = state.selectedList,
                            pendingCheckedItems = viewModel.pendingCheckedItems.value,
                            onPromptUncheckAll = viewModel::uncheckAllInList,
                            onPromptEditItem = { item -> viewModel.openEditListItemSheet(item, state.selectedList) },
                            onPromptDeleteItem = viewModel::deleteListItem,
                            onSetChecked = viewModel::setItemChecked,
                            onSetHighlighted = viewModel::setItemHighlighted,
                            onSaveIsCheckedHidden = { list, hidden -> viewModel.updateListNoItems(list.copy(isCheckedExpanded = hidden)) }
                        )
                    }
                }

                EditListSheet(
                    isOpen = state.isEditListSheetOpen,
                    editList = state.editListTarget,
                    onDismiss = viewModel::closeEditListSheet,
                    onSave = { list ->
                        viewModel.updateListNoItems(list)
                        viewModel.closeEditListSheet()
                    }
                )

                EditItemSheet(
                    isOpen = state.isEditListItemSheetOpen,
                    editListItem = state.editListItemTarget,
                    onDismiss = viewModel::closeEditListItemSheet,
                    list = state.editListItemTargetList,
                    onSave = { item ->
                        viewModel.updateListItem(item)
                        viewModel.closeEditListItemSheet()
                    }
                )

                ShareListSheet(
                    isOpen = state.isShareListSheetOpen,
                    viewModel = viewModel,
                    list = state.shareListTarget,
                    onDismiss = viewModel::closeShareListSheet,
                )

                ScanShareCodeSheet(
                    isOpen = state.isScanShareCodeSheetOpen,
                    onDismiss = viewModel::closeScanShareCodeSheet,
                    onCodeScanned = viewModel::processScannedShareCode
                )
            }
        }
    }
}