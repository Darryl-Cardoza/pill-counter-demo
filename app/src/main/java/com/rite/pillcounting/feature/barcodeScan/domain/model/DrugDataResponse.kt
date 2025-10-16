package com.rite.pillcounting.feature.barcodeScan.domain.model

import com.squareup.moshi.Json

/**
 * Root response wrapper for the backend `/drugs/ndc/{ndc}` API.
 */
data class DrugDataResponse(
    val status: Int,
    @Json(name = "is_success") val isSuccess: Boolean,
    val message: String?,
    val token: String?,
    val data: DrugDataWrapper?
)

/**
 * Wrapper for "data" field that holds the actual drug details.
 */
data class DrugDataWrapper(
    val drug: DrugResult?
)

/**
 * Models a single drug object inside "data.drug".
 */
data class DrugResult(
    @Json(name = "product_ndc")
    val productNdc: String?,

    @Json(name = "package_ndc")
    val packageNdc: String?,

    @Json(name = "generic_name")
    val genericName: String?,

    @Json(name = "brand_name")
    val brandName: String?,

    val strength: String?,
    @Json(name = "dosage_form")
    val dosageForm: String?,
    val description: String?,
    @Json(name = "manufacturer_name")
    val manufacturerName: String?,
    @Json(name = "product_type")
    val productType: String?,
    @Json(name = "class")
    val drugClass: String?,

    @Json(name = "theraupetic_id")
    val therapeuticId: String?,

    @Json(name = "specific_product_id")
    val specificProductId: String?
)
