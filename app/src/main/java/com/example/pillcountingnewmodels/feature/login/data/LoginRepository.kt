package com.example.pillcountingnewmodels.feature.login.data


import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun login(username: String, password: String): Boolean {
        return userDao.login(username, password) != null
    }

    suspend fun register(username: String, password: String) {
        userDao.insertUser(UserEntity(username, password))
    }
}