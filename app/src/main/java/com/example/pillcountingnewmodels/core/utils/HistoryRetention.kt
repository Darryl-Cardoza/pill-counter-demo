package com.example.pillcountingnewmodels.core.utils

object HistoryRetention { //TODO(Change in the viewmodel to get it from the api for the second phase)
    val optionsDays = listOf(7, 15, 30, 60, 90)

    // Map days to index in string-array (or directly to display string if you want)
    fun getTrailingText(days: Int, displayStrings: List<String>): String {
        return when (days) {
            7 -> displayStrings[0]
            15 -> displayStrings[1]
            30 -> displayStrings[2]
            60 -> displayStrings[3]
            90 -> displayStrings[4]
            else -> "$days days"
        }
    }
}
