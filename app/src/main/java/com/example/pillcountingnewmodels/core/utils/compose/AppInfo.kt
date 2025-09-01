package com.example.pillcountingnewmodels.core.utils.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
        Image(
            painter = painterResource(id = R.drawable.logo), // replace with your image
            contentDescription = "Pill Counting Logo",
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = context.getString(R.string.rite_title),
            style = MaterialTheme.typography.bodySmall,
            letterSpacing = 2.5.sp,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "VERSION ${context.getString(R.string.app_version_name)}",
            style = MaterialTheme.typography.bodySmall,
            letterSpacing = 2.5.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
