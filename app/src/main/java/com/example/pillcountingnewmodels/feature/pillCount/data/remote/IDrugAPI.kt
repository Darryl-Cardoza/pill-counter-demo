package com.example.pillcountingnewmodels.feature.pillCount.data.remote

import com.example.pillcountingnewmodels.feature.pillCount.domain.model.DrugDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Defines the API endpoints for fetching drug information using Retrofit.
 * This interface outlines the HTTP requests to the openFDA API.
 */
interface IDrugAPI {

    /**
     * Fetches drug labeling information based on a National Drug Code (NDC).
     * This searches for an exact match within the openfda.ndc field.
     *
     * @param ndc The exact NDC to search for.
     * @return A [DrugDataResponse] containing the search results.
     */
    @GET("drug/ndc.json")
    suspend fun getDrugInfoByNdc(@Query("search") ndc: String): DrugDataResponse
}
