package com.example.pillcountingnewmodels.core.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents an individual error detail in case of failure.
 */
@JsonClass(generateAdapter = true)
data class ErrorDetail(
    @Json(name = "loc")
    val loc: List<Any>? = null,
    @Json(name = "msg")
    val msg: String? = null,
    @Json(name = "type")
    val type: String? = null
)