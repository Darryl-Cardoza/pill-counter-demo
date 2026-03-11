package com.rite.pillcounting.feature.unsyncedTransaction.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.buttonCornerRadius
import com.rite.pillcounting.core.utils.constants.Dimens.buttonHeight
import com.rite.pillcounting.core.utils.constants.Dimens.extraSmall
import com.rite.pillcounting.core.utils.constants.Dimens.small
import com.rite.pillcounting.feature.countResume.domain.model.CountItem
import com.rite.pillcounting.ui.theme.AppTheme

@Composable
fun UnsyncedTransactionList(
    items: List<CountItem>,
    onButtonClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = small, end = small, bottom = extraSmall)
    ) {

        // Scrollable list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                UnsyncedTransactionListCard(
                    item = item
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(buttonCornerRadius),
                colors = androidx.compose.material.ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = AppTheme.extendedColors.textColor
                ),
                modifier = Modifier
                    .height(buttonHeight)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.sync_all),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}


