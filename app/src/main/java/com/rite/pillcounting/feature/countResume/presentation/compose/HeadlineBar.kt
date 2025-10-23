package com.rite.pillcounting.feature.countResume.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.ui.theme.AppTheme

@Composable
fun HeadlineBar(
    navController: NavController,
    title: String,
    searchQuery: String,
    showSearch: Boolean,
    onSearchClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showSearch) {
            // 🔍 Search mode
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(navController)

                Spacer(Modifier.width(6.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { onSearchChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.searchWithDots)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = AppTheme.extendedColors.textColor,
                        focusedTextColor = AppTheme.extendedColors.textColor,
                        unfocusedTextColor = AppTheme.extendedColors.textColor,
                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close_app),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onSearchClick() }
            )
        } else {
            // 🧭 Normal mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(navController)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.extendedColors.textColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(R.string.cd_search),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onSearchClick() }
                )

                Spacer(Modifier.width(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = stringResource(R.string.cd_select_items_to_delete),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDeleteClick() }
                )
            }
        }
    }
}
