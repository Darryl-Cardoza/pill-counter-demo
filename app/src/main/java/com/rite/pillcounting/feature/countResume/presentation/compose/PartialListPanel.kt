package com.rite.pillcounting.feature.countResume.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonSingleSelectDialog
import com.rite.pillcounting.core.utils.constants.Dimens.extraSmall
import com.rite.pillcounting.core.utils.constants.Dimens.small
import com.rite.pillcounting.feature.countResume.domain.data.ResumeEvent
import com.rite.pillcounting.feature.countResume.domain.data.ResumeEventFactory
import com.rite.pillcounting.feature.countResume.domain.model.CountItem
import com.rite.pillcounting.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <E : ResumeEvent> PartialListPanel(
    items: List<CountItem>,
    selectedItems: List<CountItem>,
    isMultiSelectMode: Boolean,
    searchQuery: String,
    onEvent: (E) -> Unit,
    eventFactory: ResumeEventFactory<E>,
    countType: String
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(DeleteMode.None) }
    var pendingItem by remember { mutableStateOf<CountItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = small, end = small, bottom = extraSmall)
    ) {
        if (isMultiSelectMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.selected_items_count, selectedItems.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.extendedColors.textColor
                )
                Text(
                    text = stringResource(R.string.tap_to_delete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        val filteredItems = remember(searchQuery, items) {
            if (searchQuery.isBlank()) items
            else items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                CountRow(
                    item = item,
                    multiSelectMode = isMultiSelectMode,
                    isSelected = selectedItems.contains(item),
                    countType = countType,
                    onSelectChange = { onEvent(eventFactory.selectItem(item)) },
                    onMoreClick = {
                        pendingItem = item
                        showMoreDialog = true
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        val message = when (deleteMode) {
            DeleteMode.Single -> stringResource(R.string.delete_item_text)
            DeleteMode.Multi -> stringResource(R.string.delete_selected_items_text)
            else -> ""
        }
        CommonDialog(
            message = message,
            confirmText = stringResource(R.string.yes),
            cancelText = stringResource(R.string.no),
            onConfirm = {
                when (deleteMode) {
                    DeleteMode.Single -> pendingItem?.let {
                        onEvent(eventFactory.itemSwipedToDelete(it))
                    }
                    DeleteMode.Multi -> onEvent(eventFactory.deleteClicked())
                    else -> {}
                }
                showDeleteDialog = false
                pendingItem = null
                deleteMode = DeleteMode.None
            },
            onCancel = {
                showDeleteDialog = false
                pendingItem = null
                deleteMode = DeleteMode.None
            }
        )
    }

    if (showMoreDialog && pendingItem != null) {
        val options = listOf(
            stringResource(R.string.resume).uppercase(),
            stringResource(R.string.force_complete).uppercase(),
            stringResource(R.string.delete).uppercase()
        )
        CommonSingleSelectDialog(
            title = stringResource(R.string.select_option).uppercase(),
            options = options,
            selectedIndex = 0,
            onCancel = { showMoreDialog = false; pendingItem = null },
            onOk = { index ->
                pendingItem?.let { item ->
                    when (index) {
                        0 -> onEvent(eventFactory.resumeTransaction(item))
                        1 -> onEvent(eventFactory.forceCompleteTransaction(item))
                        2 -> onEvent(eventFactory.itemSwipedToDelete(item))
                    }
                }
                showMoreDialog = false
                pendingItem = null
            }
        )
    }
}

private enum class DeleteMode { None, Single, Multi }
