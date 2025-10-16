package com.rite.pillcounting.feature.countResume.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.extraSmall
import com.rite.pillcounting.core.utils.constants.Dimens.small
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonSingleSelectDialog
import com.rite.pillcounting.feature.countResume.domain.data.ResumeEvent
import com.rite.pillcounting.feature.countResume.domain.data.ResumeEventFactory
import com.rite.pillcounting.feature.countResume.domain.model.CountItem
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * Displays the right panel of the count resume screen.
 *
 * Features:
 * - Header actions (search, toggle multi-select, delete).
 * - List of [CountItem]s with support for swipe-to-delete and multi-select.
 * - Delete confirmation dialog for both single-item and bulk deletes.
 *
 * @param items List of count items to render.
 * @param selectedItems Items currently selected in multi-select mode.
 * @param isMultiSelectMode Whether multi-select mode is active.
 * @param onEvent Callback to propagate events to the ViewModel.
 * @param eventFactory Factory for creating strongly-typed [ResumeEvent]s.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <E : ResumeEvent> PartialListPanel(
    items: List<CountItem>,
    selectedItems: List<CountItem>,
    isMultiSelectMode: Boolean,
    onEvent: (E) -> Unit,
    eventFactory: ResumeEventFactory<E>,
    countType: String
) {

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(DeleteMode.None) }
    var pendingItem by remember { mutableStateOf<CountItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = small, end = small, bottom = extraSmall)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                // Close multi-select mode
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close_selection),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onEvent(eventFactory.closeMultiSelectMode()) }
                )

                Spacer(Modifier.width(16.dp))

                // Delete selected items (with confirmation)
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = stringResource(R.string.cd_delete_selected),
                    tint = if (selectedItems.isNotEmpty()) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = selectedItems.isNotEmpty()) {
                            deleteMode = DeleteMode.Multi
                            showDeleteDialog = true
                        }
                )
            } else {
                if (showSearch) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.searchWithDots)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,

                            // Only show bottom line
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                            unfocusedIndicatorColor = Color.Gray,    // grey underline when not focused
                            disabledIndicatorColor = Color.Transparent,

                            focusedTextColor = AppTheme.extendedColors.textColor,
                            unfocusedTextColor = AppTheme.extendedColors.textColor,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    LaunchedEffect(showSearch) {
                        if (showSearch) {
                            focusRequester.requestFocus()
                        }
                    }

                }

                Icon(
                    painter = if (showSearch) {
                        rememberVectorPainter(Icons.Default.Close)
                    } else {
                        painterResource(id = R.drawable.search)
                    },
                    contentDescription = stringResource(R.string.cd_search),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            if (showSearch) {
                                searchQuery = ""
                            }
                            showSearch = !showSearch
                        }
                )

                if (!showSearch) {
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = stringResource(R.string.cd_select_items_to_delete),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onEvent(eventFactory.toggleMultiSelectMode()) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------------- Counts List ---------------- */
        val filteredItems = remember(searchQuery, items) {
            if (searchQuery.isBlank()) {
                items
            } else {
                items.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
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

    /* ---------------- Delete Confirmation Dialog ---------------- */
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
            selectedIndex = null,
            onCancel = { showMoreDialog = false; pendingItem = null },
            onOk = { selectedIndex ->
                pendingItem?.let { item ->
                    when (selectedIndex) {
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

/**
 * Tracks which delete mode the confirmation dialog is for.
 */
private enum class DeleteMode {
    None, Single, Multi
}

