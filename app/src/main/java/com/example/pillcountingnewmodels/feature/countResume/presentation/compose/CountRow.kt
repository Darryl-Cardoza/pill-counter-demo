package com.example.pillcountingnewmodels.feature.countResume.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem

/**
 * Row representing a single count item in the Partial/Fixed Resume screen.
 *
 * Supports:
 * - Multi-select mode with checkboxes.
 * - Resume action button/icon.
 * - Text overflow handling for long names and dates.
 *
 * @param item The count item to display.
 * @param multiSelectMode Whether multi-select mode is active.
 * @param isSelected Whether this item is currently selected.
 * @param onSelectChange Callback triggered when the selection changes.
 * @param onResumeClick Callback triggered when the resume action is clicked.
 */
@Composable
fun CountRow(
    item: CountItem,
    multiSelectMode: Boolean,
    isSelected: Boolean,
    onSelectChange: () -> Unit,
    onResumeClick: () -> Unit
) {
    val iconColor = Color(0xFF00BCD4)
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val isDarkMode = isSystemInDarkTheme()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color.Black else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = multiSelectMode) { onSelectChange() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ---------------- Multi-select Checkbox ----------------
            if (multiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectChange() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // ---------------- Item Icon ----------------
            Icon(
                imageVector = Icons.Default.AspectRatio,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ---------------- Item Details ----------------
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ---------------- Quantity ----------------
            Text(
                text = item.quantity.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ---------------- Resume Action ----------------
            if (!multiSelectMode) {
                if (isPortrait) {
                    IconButton(onClick = onResumeClick) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Resume",
                            tint = Color(0xFFFD82B5),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onResumeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFD82B5),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .width(90.dp)
                    ) {
                        Text(
                            text = "RESUME",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
