package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
@Composable
fun InformationPanelSection(
    uiState: PillScanningUiState,
    viewModel: PillScanningViewModel,
    onEvent: (PillScanningEvent) -> Unit,
    filteredPillCount: Int
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()
    val totalCount = uiState.txnDetailHistory.sumOf { it.count }
    var showListOfTxnDetails by remember { mutableStateOf(true) }

    val countDisplay = when (uiState.scanType) {
        "FIXED" -> "Total $totalCount/${uiState.targetCount}"
        else -> "Total $totalCount"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.drugName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.extendedColors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                CircularCountIndicator(
                    count = filteredPillCount,
                    viewModel = viewModel
                )

                Text(
                    text = countDisplay,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.extendedColors.textColor,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCountIndicator(
                    count = filteredPillCount,
                    modifier = Modifier.padding(all = 10.dp),
                    viewModel = viewModel
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.drugName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.extendedColors.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = countDisplay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.extendedColors.textColor,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!isLandscape) Modifier.weight(1f, fill = true) else Modifier.height(
                        90.dp
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showListOfTxnDetails,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut()
            ) {
                LazyRow(
                    state = listState,
                    reverseLayout = true,
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(),
                ) {
                    itemsIndexed(
                        uiState.txnDetailHistory,
                        key = { _, txn -> txn.txnDetailId }
                    ) { index, txn ->
                        Chip(
                            shouldHighlight = index == 0,
                            txnDetail = txn,
                            index = uiState.txnDetailHistory.size - index,
                            onDelete = { id ->
                                onEvent(PillScanningEvent.TransactionDetailDeleted(id))
                            }
                        )
                    }
                }
            }

        }



        ActionButtons(onEvent, filteredPillCount, onListClicked = {
            showListOfTxnDetails = !showListOfTxnDetails
        })
    }

    LaunchedEffect(uiState.txnDetailHistory.size) {
        if (uiState.txnDetailHistory.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
}
