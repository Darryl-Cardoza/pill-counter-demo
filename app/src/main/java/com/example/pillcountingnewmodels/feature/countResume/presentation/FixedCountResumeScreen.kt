package com.example.pillcountingnewmodels.feature.countResume.presentation

import Screen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.feature.countResume.domain.data.FixedCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.RegularCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.ResumeEventFactory
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem
import com.example.pillcountingnewmodels.feature.countResume.presentation.compose.PartialListPanel
import com.example.pillcountingnewmodels.feature.countResume.presentation.viewmodel.CountsViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun FixedCountResumeScreen(
    navController: NavController,
    viewModel: CountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.fixedUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToPillCount -> {
                    navController.navigate(Screen.PillCount.createRoute(event.countType.toString())){}
                }

                NavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // Factory mapping FixedCountsEvent
    val fixedEventFactory = object : ResumeEventFactory<FixedCountsEvent> {
        override fun toggleMultiSelectMode() = FixedCountsEvent.ToggleMultiSelectMode
        override fun closeMultiSelectMode() = FixedCountsEvent.CloseMultiSelectMode
        override fun deleteClicked() = FixedCountsEvent.DeleteClicked
        override fun itemSwipedToDelete(item: CountItem) = FixedCountsEvent.ItemSwipedToDelete(item)
        override fun forceCompleteTransaction(item: CountItem) = FixedCountsEvent.ForceCompleteTransaction(item)
        override fun selectItem(item: CountItem) = FixedCountsEvent.SelectItem(item)
        override fun resumeTransaction(item: CountItem) = FixedCountsEvent.resumeTransaction(item)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.primaryBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BackButton(navController)

            Text(
                text = stringResource(R.string.fixed_partial_count_title).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extendedColors.textColor
            )
        }
        PartialListPanel(
            items = uiState.fixedCounts,
            selectedItems = uiState.selectedItems,
            isMultiSelectMode = uiState.isMultiSelectMode,
            onEvent = viewModel::onFixedEvent,
            eventFactory = fixedEventFactory,
            countType = CountType.FIXED.toString()
        )
    }
}
