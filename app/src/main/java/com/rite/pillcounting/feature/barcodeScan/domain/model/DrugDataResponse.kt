package com.rite.pillcounting.feature.barcodeScan.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DrugDataResponse(
    val status: Int? = null,
    val is_success: Boolean? = null,
    val message: String? = null,
    val token: String? = null,
    val data: DrugComparisonData? = null
)

@Serializable
data class DrugComparisonData(
    val is_ndc_same: Boolean? = null,
    val is_ndc_equivalent: Boolean? = null,
    val target_ndc: NdcDrugInfo? = null,
    val scanned_ndc: NdcDrugInfo? = null
)

@Serializable
data class NdcDrugInfo(
    val package_ndc: String? = null,
    val product_ndc: String? = null,
    val splittable: Boolean? = null,
    val standard_name: String? = null,
    val active_ingredients: List<ActiveIngredient>? = null,
    val dea_schedule: String? = null,
    val dosage_form: String? = null,
    val lookup_name: String? = null,
    val manufacturer: String? = null,
    val route: List<String>? = null,
    val therapeutic_rxclass: TherapeuticRxClass? = null,
    val therapeutic_fda: TherapeuticFda? = null,
    val image: DrugImage? = null,
    val updated_at: String? = null
)

@Serializable
data class ActiveIngredient(
    val name: String? = null,
    val strength: String? = null
)

@Serializable
data class TherapeuticRxClass(
    val primary_class: String? = null,
    val secondary_classes: List<String>? = null,
    val source: String? = null
)

@Serializable
data class TherapeuticFda(
    val note: String? = null
)

@Serializable
data class DrugImage(
    val link: String? = null
)

//package com.rite.pillcounting.feature.barcodeScan.domain.model
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class DrugDataResponse(
//    val data: Data? = null,
//    val is_success: Boolean? = null,
//    val message: String? = null,
//    val status: Int? = null,
//    val token: String? = null
//)
//
//@Serializable
//data class Data(
//    val _match_type: String? = null,
//    val active_ingredients: List<ActiveIngredient>? = null,
//    val brand_name: String? = null,
//    val `class`: DrugClass? = null,
//    val dosage_form: String? = null,
//    val generic_name: String? = null,
//    val package_ndc: String? = null,
//    val packaging: List<Packaging>? = null,
//    val product_ndc: String? = null,
//    val specific_product_id: String? = null,
//    val theraupetic_id: String? = null
//)
//
//@Serializable
//data class ActiveIngredient(
//    val name: String? = null,
//    val strength: String? = null
//)
//
//@Serializable
//data class DrugClass(
//    val pharm_class: List<String>? = null,
//    val pharm_class_cs: List<String>? = null,
//    val pharm_class_epc: List<String>? = null,
//    val pharm_class_moa: List<String>? = null,
//    val pharm_class_pe: List<String>? = null
//)
//
//@Serializable
//data class Packaging(
//    val description: String? = null,
//    val marketing_start_date: String? = null,
//    val package_ndc: String? = null,
//    val sample: Boolean? = null
//)
