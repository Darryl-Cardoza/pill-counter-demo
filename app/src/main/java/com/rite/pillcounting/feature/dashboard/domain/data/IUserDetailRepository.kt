package com.rite.pillcounting.feature.dashboard.domain.data

import com.rite.pillcounting.core.models.ApiResponse
import com.rite.pillcounting.feature.dashboard.domain.model.UserDetail
import com.rite.pillcounting.feature.dashboard.domain.model.UserDetailResponse

/**
 * Contract for authentication-related data operations.
 */
interface IUserDetailRepository {


    /**
     * Fetches the details of the currently authenticated user.
     *
     * @param token The Bearer token for authorization.
     * @return Flow emitting [UserDetailResponse] on success or error.
     */
    suspend fun getUserDetail(token: String): Result<ApiResponse<UserDetail>>
}
