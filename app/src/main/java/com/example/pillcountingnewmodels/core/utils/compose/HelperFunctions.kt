package com.example.pillcountingnewmodels.core.utils.compose

import Screen
import com.example.pillcountingnewmodels.core.room.models.enums.CountStatus
import com.example.pillcountingnewmodels.core.room.models.enums.CountType
import com.example.pillcountingnewmodels.core.room.models.dtos.StatusTypeCount
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.menu.domain.model.CountBuckets
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE

/**
 * A collection of utility functions used across the Compose and ViewModel layers.
 *
 * Includes:
 * - String masking helpers (e.g., email masking).
 * - Navigation helpers to determine initial routes.
 * - Mapping helpers to transform database aggregation results into UI models.
 */
object HelperFunctions {

    /**
     * Masks an email address by preserving a portion of the local part (before `@`) and
     * replacing the remainder with stars (`*`), while leaving the domain intact.
     *
     * Examples:
     * ```
     * maskEmail("andrew@example.com")   // "an***@example.com"
     * maskEmail("a@x.com")              // "a***@x.com"
     * maskEmail("john.doe@mail.co")     // "jo****@mail.co"
     * ```
     *
     * Edge cases:
     * - If the input is `null` or blank → returns an empty string.
     * - If no `@` is found or it's malformed → falls back to a masked prefix only.
     *
     * @param email The original email to mask.
     * @param showFirst Number of characters to keep visible at the start of the local part. Default = `2`.
     * @param minStars Minimum number of `*` characters to display after the visible prefix. Default = `3`.
     * @return The masked email string.
     */
    fun maskEmail(
        email: String?,
        showFirst: Int = 2,
        minStars: Int = 3
    ): String {
        if (email.isNullOrBlank()) return ""
        val trimmed = email.trim()

        val atIndex = trimmed.indexOf('@')
        if (atIndex <= 0 || atIndex == trimmed.length - 1) {
            // Not a valid email → mask only prefix
            return if (trimmed.length <= showFirst) {
                trimmed
            } else {
                trimmed.take(showFirst) + "*".repeat(minStars)
            }
        }

        val local = trimmed.substring(0, atIndex)
        val domain = trimmed.substring(atIndex + 1)

        // Prefix of local part to show
        val visible = if (local.length <= showFirst) local else local.take(showFirst)

        // Stars count ensures at least minStars, scaled with local length
        val starsCount = maxOf(minStars, (local.length - visible.length).coerceAtLeast(minStars))
        val stars = "*".repeat(starsCount)

        return "$visible$stars@$domain"
    }

    /**
     * Determines the start destination for navigation based on login state.
     *
     * - If the user is logged in (based on [PreferenceHelper]), navigate to Dashboard.
     * - If not, navigate to the authentication graph.
     *
     * @param preferenceHelper A wrapper for user/session preferences.
     * @return Navigation route string (`Screen.Dashboard.route` or [AUTH_GRAPH_ROUTE]).
     */
    fun getStartDestination(preferenceHelper: PreferenceHelper): String {
        return if (preferenceHelper.isUserLoggedIn()) {
            Screen.Dashboard.route
        } else {
            AUTH_GRAPH_ROUTE
        }
    }

    /**
     * Maps aggregated dashboard rows from the database into structured [CountBuckets].
     *
     * Iterates over [StatusTypeCount] rows (grouped by [CountType] and [CountStatus]) and
     * separates them into:
     * - Fixed → Completed / Partial
     * - Regular → Completed / Partial
     *
     * @param rows List of aggregated transaction counts grouped by status and type.
     * @return [CountBuckets] with distributed counts for Fixed & Regular categories.
     */
    fun mapCounts(rows: List<StatusTypeCount>): CountBuckets {
        var fixedCompleted = 0
        var fixedPartial = 0
        var regularCompleted = 0
        var regularPartial = 0

        rows.forEach { row ->
            when (row.countType) {
                CountType.FIXED -> when (row.status) {
                    CountStatus.COMPLETED -> fixedCompleted = row.cnt
                    CountStatus.PARTIAL -> fixedPartial = row.cnt
                    else -> Unit
                }

                CountType.REGULAR -> when (row.status) {
                    CountStatus.COMPLETED -> regularCompleted = row.cnt
                    CountStatus.PARTIAL -> regularPartial = row.cnt
                    else -> Unit
                }
            }
        }

        return CountBuckets(
            fixedCompleted = fixedCompleted,
            fixedPartial = fixedPartial,
            regularCompleted = regularCompleted,
            regularPartial = regularPartial
        )
    }
}
