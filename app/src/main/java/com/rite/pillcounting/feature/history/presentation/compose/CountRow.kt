package com.rite.pillcounting.feature.history.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rite.pillcounting.R
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.toFormattedDate
import com.rite.pillcounting.feature.history.domain.model.TxnWithDrugDto
import com.rite.pillcounting.ui.theme.AppTheme
import java.io.File

/**
 * Row item representing a single medicine count entry in history.
 * Purely UI – all data (timestamps, count, etc.) is passed in from the ViewModel.
 *
 * @param rowData Data object for this row.
 * @param appTheme App theme wrapper for extended colors.
 */
@Composable
fun CountRow(
    rowData: TxnWithDrugDto,
    onTxnClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .clickable(onClick = onTxnClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine thumbnail/logo
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(56.dp) // slightly larger to make room for border
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        color = colorResource(R.color.border_gray),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val hasImage = !rowData.barcodeImage.isNullOrEmpty()

                val painter = if (hasImage) {
                    val file = File(rowData.barcodeImage ?: "")
                    rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .placeholder(R.drawable.bottle)
                            .error(R.drawable.bottle)
                            .build()
                    )
                } else {
                    painterResource(R.drawable.bottle)
                }
                val imageModifier = if (hasImage) {
                    Modifier
                        .fillMaxSize() // full container for placeholder
                        .clip(RoundedCornerShape(8.dp))
                } else {
                    Modifier
                        .size(36.dp) // smaller for cropped image
                        .clip(RoundedCornerShape(8.dp))
                }
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = if (hasImage) ContentScale.Crop else ContentScale.Fit,
                    modifier = imageModifier
                )
            }

            Spacer(Modifier.width(12.dp))

            // Medicine name + timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rowData.drugName.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.extendedColors.textColor
                )
                Text(
                    text = rowData.createdAt.toFormattedDate(),
                    fontSize = 12.sp,
                    color = AppTheme.extendedColors.textColor
                )
            }

            val iconRes = when {
                rowData.status == CountStatus.PARTIAL -> R.drawable.partial
                rowData.status == CountStatus.COMPLETED && !rowData.note.isNullOrBlank() -> R.drawable.notes
                else -> null
            }

            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null, // or provide a description if needed
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }



            Spacer(Modifier.width(12.dp))

            // Count value
            Text(
                text = rowData.pillCount.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extendedColors.textColor,
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
