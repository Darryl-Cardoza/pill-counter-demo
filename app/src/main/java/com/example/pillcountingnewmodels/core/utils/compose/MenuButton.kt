package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.appBarIconsPadding

@Composable
fun MenuButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    backIcon: Int = R.drawable.menu,
) {
    IconButton(
        onClick = { },
        modifier = modifier.padding(appBarIconsPadding)
    ) {
        Icon(
            painter = painterResource(id = backIcon),
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
