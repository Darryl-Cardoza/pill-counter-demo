package com.example.pillcountingnewmodels.feature.countResume.domain.model

import com.example.pillcountingnewmodels.R
import java.util.UUID

/**
 * Represents a single item in a partial or fixed count list.
 *
 * This class encapsulates the essential information about the item,
 * including its unique identifier, name, quantity, date of counting,
 * and an optional image resource.
 *
 * @property id Unique identifier for the item. Defaults to a randomly generated UUID.
 * @property name Name of the medicine or item.
 * @property quantity The quantity counted for this item.
 * @property date The timestamp when this count was recorded.
 * @property image Drawable resource ID for the item's icon. Defaults to [R.drawable.logo].
 */
data class CountItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int,
    val date: String,
    val image: Int = R.drawable.logo
)
