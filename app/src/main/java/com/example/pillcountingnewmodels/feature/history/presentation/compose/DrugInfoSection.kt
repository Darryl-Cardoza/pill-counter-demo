package com.example.pillcountingnewmodels.feature.history.presentation.compose

import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton

@Composable
fun DrugInfoSection(
    ndc: String,
    images: List<Int>,
    expiry: String,
    lotNo: Any,
    date: String,
    time: String,
    note: String?,
) {
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(0.85f)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(0.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .padding(start = 20.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageWithCount()
                        Spacer(Modifier.height(15.dp))
                        HistoryNote(note)
                    }

                    Box(
                        modifier = Modifier.weight(0.4f),
                        contentAlignment = Alignment.Center
                    ) {
                        HistoryDrugDetails(
                            ndc = ndc,
                            expiry = expiry,
                            lotNo = lotNo.toString(),
                            date = date,
                            time = time
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .weight(0.2f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    HollowButton(
                        text = stringResource(R.string.delete).uppercase(),
                        onClick = {},
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 20.dp)
                    )

                    FilledButton(
                        text = stringResource(R.string.ok).uppercase(),
                        onClick = {},
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 20.dp)
                    )
                }
            }


            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxHeight()
            ) {
                TransactionDetailsList(imageResources = images)
            }
        }

    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ImageWithCount()

            HistoryDrugDetails(
                ndc = ndc,
                expiry = expiry,
                lotNo = lotNo.toString(),
                date = date,
                time = time
            )

            HistoryNote(note)

            TransactionDetailsList(imageResources = images)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = xxLarge, start = medium, end = medium),

                horizontalArrangement = Arrangement.Center
            ) {
                HollowButton(
                    text = stringResource(R.string.delete).uppercase(),
                    onClick = {},
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 20.dp)

                )

                FilledButton(
                    text = stringResource(R.string.ok).uppercase(),
                    onClick = {},
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }
        }
    }

}