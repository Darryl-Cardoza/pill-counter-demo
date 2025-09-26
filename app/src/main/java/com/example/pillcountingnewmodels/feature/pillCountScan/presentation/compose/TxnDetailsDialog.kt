package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
import com.example.pillcountingnewmodels.core.utils.toFormattedDate
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.TxnDetail
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import java.time.LocalDateTime


@Composable
fun TxnDetailDialog(
    batchNumber: Int,
    onDelete: () -> Unit,
    onOk: () -> Unit,
    onDismiss: () -> Unit,
    onEvent: (ScanBarcodeEvent) -> Unit,
    details: TxnDetail
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentDateTime = remember {
        LocalDateTime.now()
    }

    Dialog(
        onDismissRequest = { onDismiss() }, properties = DialogProperties(
            usePlatformDefaultWidth = false, dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )

    ) {


        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.58f else 0.90f)
                .fillMaxHeight(if (isLandscape) 0.84f else 0.42f),

            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.extendedColors.secondaryBackground
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),


                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, top = 14.dp, bottom = 6.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Text(
                        text = "BATCH $batchNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.extendedColors.textColor,
                        modifier = Modifier
                            .weight(0.86f)
                            .wrapContentWidth(Alignment.Start)
                    )

                    // Close button
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentWidth(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back",
                            tint = AppTheme.extendedColors.textColor.copy(alpha = 1.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 11.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Image on left
                    details.thumbnail?.asImageBitmap()?.let {
                        Image(
                            painter = BitmapPainter(it),
                            contentDescription = "",
                            modifier = Modifier
                                .width(if (isLandscape) 246.dp else 160.dp)      // custom width
                                .height(if (isLandscape) 154.dp else 140.dp)     // custom height
                                .clip(RoundedCornerShape(12.dp)), // corner radius
                            contentScale = ContentScale.Crop // crops to fill the shape
                        )
                    }



                    Spacer(modifier = Modifier.width(if (isLandscape) 28.dp else 28.dp))
                    // Middle text
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.pills_count).uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary, // apply alpha here
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400)

                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        // Circle count badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)

                        ) {
                            Text(
                                text = "${details.count}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 28.sp
                                )

                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic Date
                        Text(
                            text = details.createdAt.toFormattedDate(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AppTheme.extendedColors.textColor.copy(alpha = 1f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = (Modifier.height(if (isLandscape) 20.dp else 30.dp)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = xxLarge,
                            start = if (isLandscape) 2.dp else 0.dp,
                            end = if (isLandscape) 2.dp else 0.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Left Arrow
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(320.dp)
                                .background(Color.Transparent),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        )
                    }

                    // Buttons Row
                    Row(
                        modifier = Modifier
                            .weight(1f),// spacing from arrows
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        // Hollow Button
                        OutlinedButton(
                            onClick = { /* delete */ },
                            modifier = Modifier
                                // equal space
                                .width(if (isLandscape) 100.dp else 100.dp)
                                .height(if (isLandscape) 52.dp else 52.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.cd_delete).uppercase(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center,     // ensure text aligns inside width
                                modifier = Modifier.fillMaxWidth() // takes full width inside button
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isLandscape) 28.dp else 18.dp))
                        // Filled Button
                        Button(
                            onClick = { /* ok */ },
                            modifier = Modifier
                                .width(if (isLandscape) 100.dp else 100.dp) // equal space
                                .height(if (isLandscape) 52.dp else 52.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.ok).uppercase(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center,     // ensure text aligns inside width
                                modifier = Modifier.fillMaxWidth() // takes full width inside button
                            )
                        }
                    }

                    // Right Arrow
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(320.dp)
                                .background(Color.Transparent),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        )
                    }
                }
            }

        }
    }
}


