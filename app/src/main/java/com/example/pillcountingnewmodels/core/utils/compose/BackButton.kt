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
import com.example.pillcountingnewmodels.core.utils.Dimens.extraSmall
import com.example.pillcountingnewmodels.core.utils.Dimens.small

@Composable
fun BackButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    backIcon: Int = R.drawable.back,
    onClick: (() -> Unit)? = null
) {
    IconButton(
        onClick = {
            onClick?.invoke() ?: navController.popBackStack()
        },
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

