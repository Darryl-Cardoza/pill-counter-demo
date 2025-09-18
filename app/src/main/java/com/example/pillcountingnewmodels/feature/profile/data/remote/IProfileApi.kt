package com.example.pillcountingnewmodels.feature.profile.data.remote

import com.example.pillcountingnewmodels.core.utils.URLConstant
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileDeleteResponse
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateRequest
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit service interface for profile-related API operations.
 */
interface IProfileApi {

    /**
     * Updates the user's profile information.
     *
     * @param request Profile update request body.
     * @return [ProfileUpdateResponse] containing the updated profile info.
     */
    @POST(URLConstant.UPDATE_PROFILE)
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpdateRequest
    ): ProfileUpdateResponse

    /**
     * Deletes the user's profile permanently.
     *
     * @return [ProfileDeleteResponse] indicating success or failure.
     */
    @DELETE(URLConstant.DELETE_PROFILE)
    suspend fun deleteProfile(
    ): ProfileDeleteResponse
}
