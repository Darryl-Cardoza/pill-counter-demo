package com.example.pillcountingnewmodels.feature.countResume.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEventFactory
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem

/**
 * Right panel displaying the list of count items (fixed or regular) with support for:
 * - Multi-select
 * - Swipe-to-delete
 * - Resume actions
 *
 * @param items List of CountItem to display.
 * @param selectedItems List of currently selected items (for multi-select mode).
 * @param isMultiSelectMode Flag indicating whether multi-select mode is active.
 * @param onEvent Callback to send events to the ViewModel.
 * @param eventFactory Factory to generate ResumeEvent instances.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // ---------------- Header Actions ----------------
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

                // Delete selected items
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = stringResource(R.string.cd_delete_selected),
                    tint = if (selectedItems.isNotEmpty()) Color.Red
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = selectedItems.isNotEmpty()) {
                            onEvent(eventFactory.deleteClicked())
                        }
                )
            } else {
                // Search action placeholder
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(R.string.cd_search),
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* TODO: Implement search */ }
                )

                Spacer(Modifier.width(16.dp))

                // Enable multi-select mode
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

        // ---------------- Counts List ----------------
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { newValue ->
                        if (newValue == DismissValue.DismissedToStart || newValue == DismissValue.DismissedToEnd) {
                            onEvent(eventFactory.itemSwipedToDelete(item))
                            true
                        } else false
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        // Red background with delete icon
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent, RoundedCornerShape(12.dp))
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
}
