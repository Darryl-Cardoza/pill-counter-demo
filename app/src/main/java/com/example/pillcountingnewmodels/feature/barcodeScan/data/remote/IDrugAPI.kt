package com.example.pillcountingnewmodels.feature.barcodeScan.data.remote

import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.DrugDataResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Defines the API endpoints for fetching drug information from your backend.
 */
interface IDrugAPI {

    /**
     * Fetches drug information based on a National Drug Code (NDC).
     *
     * Example:
     * GET /drugs/ndc/{ndc}
     *
     * @param ndc National Drug Code.
     * @return A [DrugDataResponse] containing the search results.
     */
    @GET("drugs/ndc/{ndc}")
    suspend fun getDrugInfoByNdc(
        @Header("Authorization") authorization: String,
        @Path("ndc") ndc: String,
    ): DrugDataResponse
}
