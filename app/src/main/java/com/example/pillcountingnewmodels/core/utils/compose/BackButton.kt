package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.extraSmall
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small

@Composable
fun BackButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    backIcon: Int = R.drawable.back,
) {
    IconButton(
        onClick = { navController.popBackStack() },
        modifier = modifier.padding(small)
    ) {
        Icon(
            painter = painterResource(id = backIcon),
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(extraSmall)
        )
    }
}
