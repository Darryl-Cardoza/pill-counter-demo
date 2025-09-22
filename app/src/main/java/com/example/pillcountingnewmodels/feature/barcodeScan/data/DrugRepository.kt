package com.example.pillcountingnewmodels.feature.barcodeScan.data

import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.barcodeScan.data.remote.IDrugAPI
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.IDrugRepository
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.DrugInfo
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the [IDrugRepository] interface that fetches
 * drug information from a remote backend API using [IDrugAPI].
 *
 * This repository is responsible for:
 * - Adding authentication headers (Bearer token from [PreferenceHelper]).
 * - Executing the network request via Retrofit.
 * - Safely handling errors (HTTP, network, unexpected).
 * - Mapping the raw API response into the [DrugInfo] domain model.
 *
 * ### Error Handling
 * - If the access token is missing, the request will not be sent and `null` is returned.
 * - `HttpException` (e.g., 401/403/500) is caught, logged, and results in `null`.
 * - `IOException` (network issues) is caught, logged, and results in `null`.
 * - Any other exceptions are caught and logged to prevent crashes.
 *
 * ### Return Contract
 * - Returns a [DrugInfo] object if data is successfully fetched and mapped.
 * - Returns `null` if authentication fails, no results are found, or an error occurs.
 *
 * @property api Retrofit API service for accessing drug endpoints.
 * @property preferenceHelper Helper for retrieving the saved access token.
 */
@Singleton
class DrugRepository @Inject constructor(
    private val api: IDrugAPI,
    private val preferenceHelper: PreferenceHelper,
) : IDrugRepository {

    /** Logger instance for this repository. */
    private val logger = AppLogger.create<DrugRepository>()

    /**
     * Retrieves drug information from the backend service using the given [ndc].
     *
     * - Injects the `Authorization: Bearer <token>` header automatically.
     * - Maps the backend response into a [DrugInfo] domain object.
     * - Provides null-safety and exception guarding to avoid app crashes.
     *
     * @param ndc National Drug Code of the drug to be fetched.
     * @return A [DrugInfo] object if the request is successful, otherwise `null`.
     */
    override suspend fun getDrugInfoByNdc(ndc: String): DrugInfo? {
        logger.i("Fetching drug info for NDC: '$ndc'")

        return try {
            val token = preferenceHelper.getAccessToken()
            if (token.isNullOrBlank()) {
                logger.e("No access token found. Aborting API call.")
                return null
            }

            val response = api.getDrugInfoByNdc(
                authorization = "Bearer $token",
                ndc = ndc
            )

            val result = response.data
            if (result == null) {
                logger.w("No result found in API response for NDC: '$ndc'")
                null
            } else {
                DrugInfo(
                    brandName = result.drug?.brandName ?: "N/A",
                    genericName = result.drug?.genericName ?: "N/A",
                    ndc = ndc
                ).also {
                    logger.i("Returning mapped DrugInfo -> $it")
                }
            }
        } catch (e: HttpException) {
            logger.e("HTTP error while fetching drug info (code=${e.code()}, message=${e.message()})", e)
            null
        } catch (e: IOException) {
            logger.e("Network error while fetching drug info", e)
            null
        } catch (e: Exception) {
            logger.e("Unexpected error while fetching drug info", e)
            null
        }
    }
}
