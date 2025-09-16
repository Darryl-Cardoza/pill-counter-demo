package com.example.pillcountingnewmodels.core.utils.compose

import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE

object HelperFunctions {

    /**
     * Masks an email so it shows a few chars at the start of the local part and the full domain.
     * Examples:
     *  - "andrew@example.com" -> "an***@example.com"
     *  - "a@x.com" -> "a***@x.com"
     *  - "john.doe@mail.example.co" -> "jo****@mail.example.co"
     *
     * @param email the original email
     * @param showFirst how many chars of the local part to keep at the start (default 2)
     * @param minStars minimum number of '*' to show between shown start and domain (default 3)
     * @return masked email or original trimmed string if input isn't a valid-looking email
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
            // not a valid email: return collapsed version
            return if (trimmed.length <= showFirst) {
                trimmed
            } else {
                trimmed.take(showFirst) + "*".repeat(minStars)
            }
        }

        val local = trimmed.substring(0, atIndex)
        val domain = trimmed.substring(atIndex + 1)

        // compute visible prefix for local part
        val visible = when {
            local.length <= showFirst -> local
            else -> local.take(showFirst)
        }

        // number of stars based on local length (at least minStars)
        val starsCount = maxOf(minStars, (local.length - visible.length).coerceAtLeast(minStars))
        val stars = "*".repeat(starsCount)

        return "$visible$stars@$domain"
    }


    fun getStartDestination(preferenceHelper: PreferenceHelper): String {
        return if (preferenceHelper.isUserLoggedIn()) {
            Screen.Dashboard.route
        } else {
            AUTH_GRAPH_ROUTE
        }
    }

}