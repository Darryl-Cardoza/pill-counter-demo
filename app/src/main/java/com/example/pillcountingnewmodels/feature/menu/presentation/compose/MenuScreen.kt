package com.example.pillcountingnewmodels.feature.menu.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE
import com.example.pillcountingnewmodels.ui.theme.AppTheme.extendedColors

@Composable
fun MenuScreen(
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.secondaryBackground)
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // Back arrow + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = stringResource(R.string.back_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(Modifier.width(24.dp))

        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(extendedColors.secondaryBackground)
        ) {
            // Menu items
            MenuItemRow(
                icon = R.drawable.fixed_count,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Fixed Count",
                completed = "21 completed",
                partial = "10 partial",
                completedTint = MaterialTheme.colorScheme.secondary,
                partialTint = MaterialTheme.colorScheme.secondary,
                completedIcon = R.drawable.tick,
                partialIcon = R.drawable.partial
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            MenuItemRow(
                icon = R.drawable.regular_count,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Regular Count",
                completed = "21 completed",
                partial = "10 partial",
                completedTint = MaterialTheme.colorScheme.primary,
                partialTint = MaterialTheme.colorScheme.primary,
                completedIcon = R.drawable.tick,
                partialIcon = R.drawable.partial
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.history,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "History",
                trailingText = "3 months",
                onClick = { navController.navigate(Screen.History.route) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.settings,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Settings",
                onClick = { navController.navigate(Screen.Settings.route) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.logout,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Logout",
                onClick = {
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}


@Composable
fun MenuItemRow(
    icon: Int,
    title: String,
    completed: String? = null,
    partial: String? = null,
    iconTint: androidx.compose.ui.graphics.Color,
    completedTint: androidx.compose.ui.graphics.Color,
    partialTint: androidx.compose.ui.graphics.Color,
    completedIcon: Int,
    partialIcon: Int
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = extendedColors.textColor
            )

            if (!isPortrait && completed != null && partial != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    StatBadge(
                        text = completed,
                        tint = completedTint,
                        icon = completedIcon,
                        hasBackground = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatBadge(
                        text = partial,
                        tint = partialTint,
                        icon = partialIcon,
                        hasBackground = true
                    )
                }
            }
        }

        if (isPortrait && completed != null && partial != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatBadge(
                    text = completed,
                    tint = completedTint,
                    icon = completedIcon,
                    hasBackground = false
                )
                StatBadge(
                    text = partial,
                    tint = partialTint,
                    icon = partialIcon,
                    hasBackground = true
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun StatBadge(
    text: String,
    tint: androidx.compose.ui.graphics.Color,
    icon: Int,
    hasBackground: Boolean
) {
    val modifier = if (hasBackground) {
        Modifier
            .height(40.dp)
            .width(120.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp)
    } else {
        Modifier.padding(horizontal = 4.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 14.sp, color = tint, maxLines = 1)
    }
}

@Composable
fun SimpleMenuRow(
    navController: NavController,
    icon: Int,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null
) {

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 16.sp, color = extendedColors.textColor)
        }
        trailingText?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = extendedColors.textColor,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable {
                        navController.navigate(Screen.Settings.route)
                    }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

}

