package com.firestormsw.listflow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.R
import com.firestormsw.listflow.data.model.ListItemModel
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.ui.icons.KeyboardArrowDown
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.TextSecondary
import com.firestormsw.listflow.ui.theme.Typography

@Composable
fun TodoList(
    list: ListModel?,
    pendingCheckedItems: Set<ListItemModel>,
    onPromptUncheckAll: (ListModel) -> Unit,
    onPromptEditItem: (ListItemModel) -> Unit,
    onPromptDeleteItem: (ListItemModel) -> Unit,
    onSetChecked: (ListItemModel, Boolean) -> Unit,
    onSetHighlighted: (ListItemModel, Boolean) -> Unit,
    onSaveIsCheckedHidden: (ListModel, Boolean) -> Unit,
) {
    if (list == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.empty_list_notice),
                textAlign = TextAlign.Center,
                style = Typography.bodyLarge,
                color = TextSecondary,
            )
        }
        return
    }

    // Animation specs
    val itemAnimationSpec = tween<IntOffset>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val itemList = list.items.partition { !it.isChecked || pendingCheckedItems.any { pendingItem -> pendingItem.id == it.id } }
    val uncheckedItemList = itemList.first.sortedWith(
        compareBy(
            { !it.isHighlighted },
            { -it.frequencyScore }
        )
    )

    var isCompletedExpanded by remember { mutableStateOf(!list.isCheckedExpanded) }
    val expandArrowRotation: Float by animateFloatAsState(
        animationSpec = tween(durationMillis = 200),
        targetValue = if (isCompletedExpanded) 180f else 0f
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // This is a workaround to fix the issue with LazyColumn scrolling to the bottom when the first item is checked
        item(key = 0) {
            Box(
                modifier = Modifier
                    .height(0.dp)
                    .padding(0.dp)
            )
        }

        // Unchecked items
        itemsIndexed(uncheckedItemList, key = { _, item -> item.id }) { _, item ->
            TodoListItem(
                item = item,
                onToggleChecked = {
                    onSetChecked(item, true)
                },
                onPromptEditItem = onPromptEditItem,
                onPromptDeleteItem = onPromptDeleteItem,
                onToggleHighlighted = {
                    onSetHighlighted(item, !item.isHighlighted)
                },
                modifier = Modifier.animateItem(placementSpec = itemAnimationSpec)
            )
        }

        // Checked item separator
        if (itemList.second.any()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(30.dp)
                        .clickable {
                            onSaveIsCheckedHidden(list, isCompletedExpanded)
                            isCompletedExpanded = !isCompletedExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.list_completed, itemList.second.size),
                        style = Typography.labelMedium,
                        color = TextSecondary,
                    )

                    Icon(
                        imageVector = KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.rotate(expandArrowRotation)
                    )
                }
            }
        }

        // Checked items
        if (!isCompletedExpanded) {
            itemsIndexed(itemList.second, key = { _, item -> item.id }) { _, item ->
                TodoListItem(
                    item = item,
                    onToggleChecked = {
                        onSetChecked(item, false)
                    },
                    onPromptEditItem = onPromptEditItem,
                    onPromptDeleteItem = onPromptDeleteItem,
                    onToggleHighlighted = {
                        onSetHighlighted(item, !item.isHighlighted)
                    },
                    modifier = Modifier.animateItem(placementSpec = itemAnimationSpec)
                )
            }
        }

        if (itemList.second.any()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "uncheck-all",
                                    linkInteractionListener = {
                                        onPromptUncheckAll(list)
                                    }
                                )) {
                                append(stringResource(R.string.uncheck_all_items))
                            }
                        },
                        style = Typography.bodyLarge.copy(
                            color = Accent,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}