package com.example.pillcountingnewmodels.feature.pillCount.domain.model

/**
 * Represents the clean, essential information about a drug for use within the app's domain layer.
 * This model separates the app's internal logic from the external API's data structure.
 *
 * @property brandName The commercial or brand name of the drug.
 * @property genericName The active ingredient or generic name of the drug.
 * @property ndc The National Drug Code.
 */
data class DrugInfo(
    val brandName: String?,
    val genericName: String?,
    val ndc: String
)
