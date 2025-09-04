package com.example.pillcountingnewmodels.core.utils.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun AppInfo(
    context: Context,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.padding(30.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Pill Counting Logo",
                modifier = Modifier.size(100.dp)
            )
        }

        Text(
            text = context.getString(R.string.rite_title),
            style = MaterialTheme.typography.bodySmall,
            letterSpacing = 1.sp,
            color = AppTheme.extendedColors.textColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${context.getString(R.string.version)} ${context.getString(R.string.app_version_name)}",
            style = MaterialTheme.typography.bodySmall,
            letterSpacing = 1.sp,
            color = AppTheme.extendedColors.textColor
        )
    }
}
