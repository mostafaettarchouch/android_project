package com.ofppt.istak.data.repository

import com.ofppt.istak.data.model.LoginRequest
import com.ofppt.istak.data.model.LoginResponse
import com.ofppt.istak.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject

import com.ofppt.istak.data.local.UserDao
import com.ofppt.istak.data.local.TokenManager

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(email, password, "AndroidApp")
        val response = apiService.login(request)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val body = response.body()!!
            userDao.insertUser(body.user)
            tokenManager.saveToken(body.token)
        }
        return response
    }
}
