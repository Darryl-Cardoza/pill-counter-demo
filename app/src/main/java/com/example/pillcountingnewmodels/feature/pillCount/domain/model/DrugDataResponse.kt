package com.example.pillcountingnewmodels.feature.pillCount.domain.model


import com.squareup.moshi.Json

/**
 * DTO that models the JSON response from the openFDA `/drug/ndc.json` endpoint.
 */
data class DrugDataResponse(

    val results: List<DrugResult>?
)

/**
 * Models a single drug object within the "results" array.
 */
data class DrugResult(
    @Json(name = "brand_name")
    val brandName: String?,

    @Json(name = "generic_name")
    val genericName: String?
)
