package com.firestormsw.listflow.data.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firestormsw.listflow.data.model.ListItemModel
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.repository.ListItemRepository
import com.firestormsw.listflow.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ulid.ULID
import javax.inject.Inject

@HiltViewModel
class SimpleListViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val listItemRepository: ListItemRepository,
    private val mqttManager: MqttManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimpleListState())
    val uiState: StateFlow<SimpleListState> = _uiState.asStateFlow()
    val lists = listRepository.getListsWithItems()

    private val _pendingCheckedItems = mutableStateOf<Set<ListItemModel>>(emptySet())
    val pendingCheckedItems: State<Set<ListItemModel>> = _pendingCheckedItems
    private var pendingMoveJob: Job? = null

    init {
        listRepository.getListsWithItems().observeForever { lists ->
            if (!lists.any()) {
                _uiState.update { it.copy(selectedList = null) }
            } else {
                val firstList = lists.first()
                viewModelScope.launch(Dispatchers.IO) {
                    val listWithItems = listRepository.getListWithItems(firstList.id)
                    if (!lists.any { it.id == _uiState.value.selectedList?.id }) {
                        _uiState.update { it.copy(selectedList = listWithItems) }
                    } else {
                        _uiState.update { it.copy(selectedList = lists.first { list -> list.id == _uiState.value.selectedList?.id }) }
                    }
                }
            }
        }
    }

    fun deleteList(list: ListModel) {
        viewModelScope.launch(Dispatchers.IO) {
            listRepository.deleteList(list)
            _uiState.update {
                it.copy(
                    snackbarMessage = "The list '${list.name}' was deleted",
                    snackbarAction = {
                        updateListNoItems(list)
                        list.items.forEach { item -> updateListItem(item) }
                    }
                )
            }
        }
    }

    fun deleteListItem(item: ListItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            listItemRepository.deleteListItem(item)
            _uiState.update {
                it.copy(
                    snackbarMessage = "The item '${item.text}' was deleted",
                    snackbarAction = {
                        updateListItem(item)
                    }
                )
            }
        }
    }

    fun setSelectedList(list: ListModel) {
        _uiState.update { it.copy(selectedList = list) }
    }

    fun setItemChecked(item: ListItemModel, checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (checked) {
                _pendingCheckedItems.value += item
            }
            listItemRepository.upsertListItem(item.copy(isChecked = checked))

            if (checked) {
                pendingMoveJob?.cancel()
                pendingMoveJob = viewModelScope.launch(Dispatchers.IO) {
                    delay(800)
                    for (pendingItem in _pendingCheckedItems.value) {
                        listItemRepository.upsertListItem(pendingItem.copy(isHighlighted = false, isChecked = true))
                    }
                    _pendingCheckedItems.value = emptySet()
                }
            }
        }
    }

    fun setItemHighlighted(item: ListItemModel, highlighted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            listItemRepository.upsertListItem(item.copy(isHighlighted = highlighted))
        }
    }

    fun uncheckAllInList(list: ListModel) {
        viewModelScope.launch(Dispatchers.IO) {
            listRepository.uncheckAllInList(list.id)
        }
    }

    fun updateListNoItems(list: ListModel) {
        viewModelScope.launch(Dispatchers.IO) {
            listRepository.upsertList(list)
            _uiState.update { it.copy(selectedList = list) }
        }
    }

    fun updateListItem(item: ListItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            listItemRepository.upsertListItem(item)
        }
    }

    fun openEditListSheet(editList: ListModel?) {
        _uiState.update { it.copy(isEditListSheetOpen = true, editListTarget = editList) }
    }

    fun closeEditListSheet() {
        _uiState.update { it.copy(isEditListSheetOpen = false, editListTarget = null) }
    }

    fun openEditListItemSheet(editListItem: ListItemModel?, targetList: ListModel?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (targetList == null) {
                val newList = ListModel(
                    id = ULID.randomULID(),
                    name = "Default",
                    isCheckedExpanded = true,
                    items = emptyList()
                )
                listRepository.upsertList(newList)

                _uiState.update {
                    it.copy(
                        isEditListItemSheetOpen = true,
                        editListItemTarget = editListItem,
                        editListItemTargetList = newList,
                        selectedList = newList
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isEditListItemSheetOpen = true,
                        editListItemTarget = editListItem,
                        editListItemTargetList = targetList
                    )
                }
            }
        }
    }

    fun closeEditListItemSheet() {
        _uiState.update { it.copy(isEditListItemSheetOpen = false, editListItemTarget = null) }
    }

    fun openShareListSheet(list: ListModel) {
        _uiState.update { it.copy(isShareListSheetOpen = true, shareListTarget = list) }
    }

    fun closeShareListSheet() {
        _uiState.update { it.copy(isShareListSheetOpen = false, shareListTarget = null) }
    }

    fun openScanShareCodeSheet() {
        _uiState.update { it.copy(isScanShareCodeSheetOpen = true) }
    }

    fun closeScanShareCodeSheet() {
        _uiState.update { it.copy(isScanShareCodeSheetOpen = false) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarAction = null, snackbarMessage = null) }
    }
}

data class SimpleListState(
    val selectedList: ListModel? = null,

    val isEditListSheetOpen: Boolean = false,
    val editListTarget: ListModel? = null,

    val isEditListItemSheetOpen: Boolean = false,
    val editListItemTarget: ListItemModel? = null,
    val editListItemTargetList: ListModel? = null,

    val isShareListSheetOpen: Boolean = false,
    val shareListTarget: ListModel? = null,

    val isScanShareCodeSheetOpen: Boolean = false,

    val snackbarAction: (() -> Unit)? = null,
    val snackbarMessage: String? = null,
)