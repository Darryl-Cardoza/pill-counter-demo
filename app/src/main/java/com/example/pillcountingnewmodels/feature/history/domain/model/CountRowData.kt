package com.example.pillcountingnewmodels.feature.history.domain.model

/**
 * Represents a single row in the medicine counts list on the History screen.
 *
 * This data class encapsulates all information needed to render a row in [CountRowData]:
 * - `name`: Display name of the medicine.
 * - `count`: Number of units counted.
 * - `iconRes`: Drawable resource ID for the associated icon/action.
 * - `formattedTimestamp`: Preformatted timestamp string to show when the count was recorded.
 *
 * All formatting and business logic (like timestamp formatting) should be handled in the ViewModel.
 *
 * @property name Name of the medicine.
 * @property count Number of units counted.
 * @property iconRes Drawable resource ID for the action icon.
 * @property formattedTimestamp Preformatted timestamp for display.
 */
data class CountRowData(
    val name: String,
    val count: Int,
    val iconRes: Int,
    val formattedTimestamp: String
)
