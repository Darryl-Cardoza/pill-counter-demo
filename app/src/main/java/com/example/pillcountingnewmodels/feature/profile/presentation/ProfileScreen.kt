import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.FloatingLabelTextField
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileField
import com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel.ProfileViewModel
import com.example.pillcountingnewmodels.ui.theme.PrimaryBackground
import com.example.pillcountingnewmodels.ui.theme.SecondaryColor

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current

    // Fields from ViewModel (same as before)
    val fields = listOf(
        ProfileField(stringResource(R.string.first_name), viewModel.firstName),
        ProfileField(stringResource(R.string.last_name), viewModel.lastName),
        ProfileField(stringResource(R.string.pharmacy_name), viewModel.pharmacyName),
        ProfileField(stringResource(R.string.phone_number), viewModel.phoneNumber, KeyboardType.Phone),
        ProfileField(stringResource(R.string.email), viewModel.email, KeyboardType.Email),
        ProfileField(stringResource(R.string.npi_number), viewModel.npi)
    )

    val fieldSetters = listOf<(String) -> Unit>(
        { viewModel.firstName = it },
        { viewModel.lastName = it },
        { viewModel.pharmacyName = it },
        { viewModel.phoneNumber = it },
        { viewModel.email = it },
        { viewModel.npi = it },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.primaryBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(navController)
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.extendedColors.textColor,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Fields
            if (isLandscape) {
                fields.chunked(2).forEachIndexed { rowIndex, pair ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEachIndexed { colIndex, field ->
                            val index = rowIndex * 2 + colIndex
                            FloatingLabelTextField(
                                value = field.value,
                                onValueChange = fieldSetters[index],
                                label = field.placeholder,
                                keyboardType = field.keyboardType,
                                imeAction = field.imeAction,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Spacer(Modifier.height(16.dp))

                fields.forEachIndexed { index, field ->
                    FloatingLabelTextField(
                        value = field.value,
                        onValueChange = fieldSetters[index],
                        label = field.placeholder,
                        keyboardType = field.keyboardType,
                        imeAction = field.imeAction,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.doNotAskAgain,
                    onCheckedChange = { viewModel.doNotAskAgain = it }
                )
                Text(stringResource(R.string.do_not_ask), color = AppTheme.extendedColors.textColor)
            }

            Spacer(Modifier.height(40.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                HollowButton(
                    text = stringResource(R.string.delete),
                    onClick = { /* delete */ },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
                HollowButton(
                    text = stringResource(R.string.skip_alt),
                    onClick = { /* skip */ },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
                ActionButtonPrimary(
                    text = stringResource(R.string.save),
                    onClick = { /* save */ },
                    useContentPadding = isLandscape,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
            }
        }
    }
}


