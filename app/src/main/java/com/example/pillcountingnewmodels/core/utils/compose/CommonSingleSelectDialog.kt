package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.Dimens.small
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun CommonSingleSelectDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int? = null, // initial selection
    onCancel: () -> Unit,
    onOk: (Int) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedIndex) }

    AlertDialog(
        onDismissRequest = {}, // disable outside click & back
        shape = RoundedCornerShape(12.dp),
        containerColor = AppTheme.extendedColors.primaryBackground,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.extendedColors.textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Options
                options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSelection = index }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = currentSelection == index,
                            onClick = { currentSelection = index },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(small))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = AppTheme.extendedColors.textColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HollowButton(
                    text = stringResource(R.string.cancel).uppercase(),
                    onClick = onCancel,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ActionButtonPrimary(
                    text = stringResource(R.string.ok).uppercase(),
                    onClick = { currentSelection?.let { onOk(it) } },
                    useContentPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}

