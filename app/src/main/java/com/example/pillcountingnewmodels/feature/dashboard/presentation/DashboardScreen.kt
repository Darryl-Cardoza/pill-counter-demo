package com.example.pillcountingnewmodels.feature.dashboard.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.dashboard.presentation.compose.FixedCountSection
import com.example.pillcountingnewmodels.feature.dashboard.presentation.compose.RegularCountSection
import com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {

    BackHandler { }

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.background(AppTheme.extendedColors.secondaryBackground)) {
        SplitResponsive(
            topOrLeft = {
                FixedCountSection(
                    completedFixedCount = uiState.completedFixedCount,
                    partialFixedCount = uiState.partialFixedCount,
                    navController = navController
                )
            },
            bottomOrRight = {
                RegularCountSection(
                    completedRegularCount = uiState.completedRegularCount,
                    partialRegularCount = uiState.partialRegularCount,
                    navController = navController
                )
            },
        )

        MenuButton(
            navController,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

