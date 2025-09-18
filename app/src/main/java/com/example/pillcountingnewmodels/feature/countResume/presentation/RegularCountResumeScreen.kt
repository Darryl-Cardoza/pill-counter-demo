package com.example.pillcountingnewmodels.feature.countResume.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.countResume.domain.data.RegularCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEventFactory
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem
import com.example.pillcountingnewmodels.feature.countResume.domain.model.RegularCountsUiState
import com.example.pillcountingnewmodels.feature.countResume.presentation.compose.LeftPanel
import com.example.pillcountingnewmodels.feature.countResume.presentation.compose.RightPanel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun RegularCountResumeScreen(
    navController: NavController,
    uiState: RegularCountsUiState,
    onEvent: (RegularCountsEvent) -> Unit
) {

    BackHandler { /* kept empty to consume back press and prevent navigation */ }

    // Factory mapping RegularCountsEvent
    val regularEventFactory = object : ResumeEventFactory<RegularCountsEvent> {
        override fun toggleMultiSelectMode() = RegularCountsEvent.ToggleMultiSelectMode
        override fun closeMultiSelectMode() = RegularCountsEvent.CloseMultiSelectMode
        override fun deleteClicked() = RegularCountsEvent.DeleteClicked
        override fun itemSwipedToDelete(item: CountItem) =
            RegularCountsEvent.ItemSwipedToDelete(item)

        override fun selectItem(item: CountItem) = RegularCountsEvent.SelectItem(item)
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
                    count = uiState.regularCounts.size,
                    headlineResId = R.string.partial_count_title,
                    bottomLabelResId = R.string.regular_partial_counts_label,
                    appTheme = AppTheme,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            },
            bottomOrRight = {
                RightPanel(
                    items = uiState.regularCounts,
                    selectedItems = uiState.selectedItems,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    onEvent = onEvent,
                    eventFactory = regularEventFactory
                )
            },
            portraitRatio = 0.40f to 0.60f,
            landscapeRatio = 0.3f to 0.7f
        )
    }
}
