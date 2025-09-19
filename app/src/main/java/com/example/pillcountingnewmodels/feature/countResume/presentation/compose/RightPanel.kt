package com.example.pillcountingnewmodels.feature.countResume.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEventFactory
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem

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
fun <E : ResumeEvent> RightPanel(
    items: List<CountItem>,
    selectedItems: List<CountItem>,
    isMultiSelectMode: Boolean,
    onEvent: (E) -> Unit,
    eventFactory: ResumeEventFactory<E>
) {
    val iconColor = Color(0xFF00BCD4)

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(DeleteMode.None) }
    var pendingItem by remember { mutableStateOf<CountItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        /* ---------------- Header Actions ---------------- */
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
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onEvent(eventFactory.closeMultiSelectMode()) }
                )

                Spacer(Modifier.width(16.dp))

                // Delete selected items (with confirmation)
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = stringResource(R.string.cd_delete_selected),
                    tint = if (selectedItems.isNotEmpty()) Color.Red
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = selectedItems.isNotEmpty()) {
                            deleteMode = DeleteMode.Multi
                            showDeleteDialog = true
                        }
                )
            } else {
                // Search icon (placeholder for now)
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(R.string.cd_search),
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* TODO: Implement search */ }
                )

                Spacer(Modifier.width(16.dp))

                // Enter multi-select mode
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = stringResource(R.string.cd_select_items_to_delete),
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onEvent(eventFactory.toggleMultiSelectMode()) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------------- Counts List ---------------- */
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { newValue ->
                        if (newValue == DismissValue.DismissedToStart) {
                            pendingItem = item
                            deleteMode = DeleteMode.Single
                            showDeleteDialog = true
                            false // stop auto-dismiss, wait for confirmation
                        } else false
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete),
                                tint = Color.White
                            )
                        }
                    },
                    dismissContent = {
                        CountRow(
                            item = item,
                            multiSelectMode = isMultiSelectMode,
                            isSelected = selectedItems.contains(item),
                            onSelectChange = { onEvent(eventFactory.selectItem(item)) },
                            onResumeClick = { /* TODO: Implement resume action */ }
                        )
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
}

/**
 * Tracks which delete mode the confirmation dialog is for.
 */
private enum class DeleteMode {
    None, Single, Multi
}
