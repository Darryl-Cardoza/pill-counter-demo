package com.example.pillcountingnewmodels.feature.dashboard.data.remote

import com.example.pillcountingnewmodels.core.utils.URLConstant
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.UserDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Interface defining authentication-related repository functions.
 */
interface IUserDetailAPI {

    /**
     * Gets authenticated user's details.
     *
     * @param authorization Bearer token header value.
     * @return Retrofit [Response] wrapping [UserDetailResponse].
     */
    @GET(URLConstant.GET_ABOUT_ME)
    suspend fun getUserDetail(
        @Header("Authorization") authorization: String
    ): Response<UserDetailResponse>
}
