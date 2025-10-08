package com.example.pillcountingnewmodels.feature.barcodeScan.domain.data

import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.DrugInfo


/**
 * Defines the contract for accessing drug data.
 * This interfaceDetail abstracts the data source, allowing for flexible implementations
 * (e.g., remote API, local database) and easier testing.
 */
interface IDrugRepository {

    /**
     * Retrieves drug information for a given National Drug Code (NDC).
     *
     * @param ndc The NDC of the drug to look up.
     * @return A [DrugInfo] object containing the drug's details, or null if not found.
     * @throws Exception if there is a network error or the API call fails.
     */
    suspend fun getDrugInfoByNdc(ndc: String): DrugInfo?
}
