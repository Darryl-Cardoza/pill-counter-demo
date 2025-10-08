import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.AppTextField
import com.example.pillcountingnewmodels.core.utils.Dimens.extraLarge
import com.example.pillcountingnewmodels.core.utils.Dimens.extraSmall
import com.example.pillcountingnewmodels.core.utils.Dimens.large
import com.example.pillcountingnewmodels.core.utils.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.Dimens.small
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun ManualDrugEntryDialog(
    onConfirm: (drugName: String, ndc: String) -> Unit,
    onDismiss: () -> Unit,
    drugNameFetched: String,
    ndcFetched: String
) {
    var drugName by remember { mutableStateOf("") }
    var ndc by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    drugName = drugNameFetched
    ndc = ndcFetched
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(medium),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(AppTheme.extendedColors.primaryBackground)
                    .padding(large)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.pill_info_add_manually).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.extendedColors.textColor,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = AppTheme.extendedColors.textColor
                        )
                    }
                }


                Spacer(modifier = Modifier.height(medium))

                Text(
                    text = stringResource(R.string.drugname),
                    fontSize = 12.sp,
                    color = AppTheme.extendedColors.textColor
                )
                Spacer(modifier = Modifier.height(extraSmall))
                AppTextField(
                    value = drugName,
                    onValueChange = {
                        drugName = it
                    },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )

                Spacer(modifier = Modifier.height(medium))

                Text(
                    text = stringResource(R.string.ndc).uppercase(),
                    fontSize = 12.sp,
                    color = AppTheme.extendedColors.textColor
                )
                Spacer(modifier = Modifier.height(extraSmall))
                AppTextField(
                    value = ndc,
                    onValueChange = {
                        ndc = it
                    },
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.height(small))

                // Show error if validation fails
                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(small))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = extraLarge, end = extraLarge),
                    horizontalArrangement = Arrangement.spacedBy(extraLarge) // automatic spacing
                ) {
                    HollowButton(
                        text = stringResource(R.string.cancel).uppercase(),
                        onClick = onDismiss,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FilledButton(
                        text = stringResource(R.string.ok).uppercase(),
                        onClick = {
                            if (drugName.isBlank() || ndc.isBlank()) {
                                errorMessage = "All fields are required"
                            } else {
                                onConfirm(drugName, ndc)
                            }
                        },
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
