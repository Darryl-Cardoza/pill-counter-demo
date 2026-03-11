package com.rite.pillcounting.feature.history.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveDp
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import java.io.File

@Composable

fun ImageWithCount(
    totalPillCount: String,
    targetCount: Int? = null,
    barcodeImage: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = medium)
    ) {
        val hasImage = !barcodeImage.isNullOrEmpty()

        val painter = if (hasImage) {
            val file = File(barcodeImage ?: "")
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

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = if (hasImage) ContentScale.Crop else ContentScale.Fit,
            modifier = Modifier
                .width(responsiveDp(160.dp))
                .height(responsiveDp(100.dp))
                .clip(RoundedCornerShape(12.dp)),
        )



        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 3.dp)
            ) {
                //target count is null -> countType is Regular
                if (targetCount == null) {
                    Text(
                        text = totalPillCount,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                        )
                    )

                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = totalPillCount,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                            )
                        )

                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .width(80.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        )

                        Text(
                            text = targetCount.toString(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                            )
                        )
                    }
                }
            }


            Text(
                text = stringResource(R.string.total_count).uppercase(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}