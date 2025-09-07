package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.feature.history.domain.model.CountRowData
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Row item representing a single medicine count entry in history.
 * Purely UI – all data (timestamps, count, etc.) is passed in from the ViewModel.
 *
 * @param rowData Data object for this row.
 * @param appTheme App theme wrapper for extended colors.
 */
@Composable
fun CountRow(
    rowData: CountRowData,
    appTheme: AppTheme
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine thumbnail/logo
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            // Medicine name + timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rowData.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = appTheme.extendedColors.textColor
                )
                Text(
                    text = rowData.formattedTimestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            Icon(
                painter = painterResource(rowData.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Count value
            Text(
                text = rowData.count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appTheme.extendedColors.textColor,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 0.8.dp
        )
    }
}
