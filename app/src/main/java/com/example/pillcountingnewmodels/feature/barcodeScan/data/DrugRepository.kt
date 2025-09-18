package com.example.pillcountingnewmodels.feature.barcodeScan.data

import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.barcodeScan.data.remote.IDrugAPI
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.DrugInfo
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.IDrugRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of the [IDrugRepository] interface.
 * It fetches drug data from the remote [DrugApi] and maps it to the domain model.
 *
 * @param api The Retrofit API service for drug data.
 */
@Singleton
class DrugRepository @Inject constructor(
    private val api: IDrugAPI
) : IDrugRepository {

    // Instantiate the logger for this class
    private val logger = AppLogger.create<DrugRepository>()

    /**
     * Retrieves drug information by making a network call to the openFDA API.
     * It formats the NDC for the query and maps the response to the [DrugInfo] domain model.
     *
     * @param ndc The NDC of the drug.
     * @return A [DrugInfo] object if found, otherwise null.
     */
    override suspend fun getDrugInfoByNdc(ndc: String): DrugInfo? {
        logger.i("Fetching drug info for NDC: '$ndc'")

        val response = api.getDrugInfoByNdc("product_ndc:\"$ndc\"")
        val result = response.results?.firstOrNull()

        if (result == null) {
            logger.w("No result found in API response for NDC: '$ndc'")
            return null
        }

        // Use a .let block to safely work with the non-null result
        return result.let { apiResult ->
            // Log what the API parsing returned (the raw data)
            logger.d("Raw parsed data from API -> brandName: '${apiResult.brandName}', genericName: '${apiResult.genericName}'")

            // Map the raw data to your domain model
            DrugInfo(
                brandName = apiResult.brandName ?: "N/A",
                genericName = apiResult.genericName ?: "N/A",
                ndc = ndc
            )
        }.also { finalDrugInfo ->
            // Log the final object that will be returned to the ViewModel
            logger.i("Returning mapped DrugInfo -> $finalDrugInfo")
        }
    }
}