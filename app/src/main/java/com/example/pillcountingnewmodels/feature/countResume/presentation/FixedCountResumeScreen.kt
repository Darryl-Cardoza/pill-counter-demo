package com.example.pillcountingnewmodels.feature.countResume.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.countResume.domain.data.FixedCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEventFactory
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem
import com.example.pillcountingnewmodels.feature.countResume.domain.model.FixedCountsUiState
import com.example.pillcountingnewmodels.feature.countResume.presentation.compose.LeftPanel
import com.example.pillcountingnewmodels.feature.countResume.presentation.compose.RightPanel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun FixedCountResumeScreen(
    navController: NavController,
    uiState: FixedCountsUiState,
    onEvent: (FixedCountsEvent) -> Unit
) {
    // Factory mapping FixedCountsEvent
    val fixedEventFactory = object : ResumeEventFactory<FixedCountsEvent> {
        override fun toggleMultiSelectMode() = FixedCountsEvent.ToggleMultiSelectMode
        override fun closeMultiSelectMode() = FixedCountsEvent.CloseMultiSelectMode
        override fun deleteClicked() = FixedCountsEvent.DeleteClicked
        override fun itemSwipedToDelete(item: CountItem) = FixedCountsEvent.ItemSwipedToDelete(item)
        override fun selectItem(item: CountItem) = FixedCountsEvent.SelectItem(item)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                LeftPanel(
                    count = uiState.fixedCounts.size,
                    headlineResId = R.string.partial_count_title,
                    bottomLabelResId = R.string.fixed_partial_counts_label,
                    appTheme = AppTheme,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            },
            bottomOrRight = {
                RightPanel(
                    items = uiState.fixedCounts,
                    selectedItems = uiState.selectedItems,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    onEvent = onEvent,
                    eventFactory = fixedEventFactory
                )
            },
            portraitRatio = 0.40f to 0.60f,
            landscapeRatio = 0.3f to 0.7f
        )
    }
}
