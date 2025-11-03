package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent
import com.rite.pillcounting.feature.pillCountScan.domain.model.PillScanningUiState
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * Displays the right-hand information panel in the pill counting screen.
 *
 * This section includes:
 * - The current pill count title
 * - Drug details (name, batch, total, etc.)
 * - The current count display with an option to add transaction details
 * - A horizontally scrolling list of recent transaction history chips
 * - Action buttons for rescan, pause, and completion
 *
 * It is a key UI component within the pill scanning workflow,
 * designed for tablet/kiosk layouts with responsive scaling.
 *
 * @param uiState The current state of the pill scanning UI.
 * @param viewModel The ViewModel managing scan state and user interactions.
 * @param onEvent Callback to send user interaction events to the ViewModel.
 * @param filteredPillCount The filtered pill count detected in the latest scan.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InformationPanelSection(
    uiState: PillScanningUiState,
    viewModel: PillScanningViewModel,
    onEvent: (PillScanningEvent) -> Unit,
    filteredPillCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(medium)
    ) {
        Text(
            text = stringResource(R.string.pill_count_title).uppercase(),
            fontSize = 16.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
            color = AppTheme.extendedColors.textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        DrugInformation(uiState)

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            CurrentCountDisplay(
                viewModel = viewModel,
                uiState = uiState,
                filteredCount = filteredPillCount
            ) {
                onEvent(PillScanningEvent.AddTransactionDetailClicked(filteredPillCount))
            }

            val listState = rememberLazyListState()

            LaunchedEffect(uiState.txnDetailHistory.size) {
                // Scroll to the newly added item (index 0, since reverseLayout = true)
                if (uiState.txnDetailHistory.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }

            LazyRow(
                state = listState,
                reverseLayout = true,
                horizontalArrangement = Arrangement.spacedBy(medium)
            ) {
                itemsIndexed(
                    uiState.txnDetailHistory,
                    key = { _, txnDetail -> txnDetail.txnDetailId }
                ) { index, txnDetail ->
                    val highlight = index == 0
                    var isVisible by remember { mutableStateOf(false) }

                    // Trigger fade+scale animation
                    LaunchedEffect(Unit) {
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Chip(
                            shouldHighlight = highlight,
                            txnDetail = txnDetail,
                            index = uiState.txnDetailHistory.size - index,
                            onDelete = { txnDetailId ->
                                onEvent(PillScanningEvent.TransactionDetailDeleted(txnDetailId))
                            }
                        )
                    }
                }
            }

        }

        ActionButtons(onEvent)
    }
}
