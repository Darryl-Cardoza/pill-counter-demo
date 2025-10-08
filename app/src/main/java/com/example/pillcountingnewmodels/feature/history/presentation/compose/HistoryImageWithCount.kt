package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.Dimens.medium
import java.io.File

@Composable

fun ImageWithCount(
    totalPillCount: String,
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
                .width(160.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp)),
        )



        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = totalPillCount, // dynamic total count
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 45.sp,
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.total_count).uppercase(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                ),
                textAlign = TextAlign.Center // Additional centering for text alignment
            )
        }


    }
}