package com.example.pillcountingnewmodels.feature.countResume.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Left panel displaying summary information about count items.
 *
 * @param count The total number of count items.
 * @param headlineResId String resource ID for the main title displayed at the top.
 * @param bottomLabelResId String resource ID for the descriptive text shown below the count number.
 * @param appTheme The app's custom theme for colors.
 * @param onBackClick Callback triggered when the back icon is clicked.
 */
@Composable
fun LeftPanel(
    count: Int,
    headlineResId: Int,
    bottomLabelResId: Int,
    appTheme: AppTheme,
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Icon(
            painter = painterResource(R.drawable.back),
            contentDescription = stringResource(R.string.back_content_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() }
        )

        Spacer(Modifier.width(24.dp))

        Text(
            text = stringResource(headlineResId),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = appTheme.extendedColors.textColor
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(Modifier.height(if (isPortrait) 20.dp else 40.dp))

        // ---------------- Count Icon & Number ----------------
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.partial),
                contentDescription = stringResource(R.string.partial_icon_description),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxSize(),
            )

            Text(
                text = count.toString(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------------- Bottom Label ----------------
        Text(
            text = stringResource(bottomLabelResId),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Default,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
