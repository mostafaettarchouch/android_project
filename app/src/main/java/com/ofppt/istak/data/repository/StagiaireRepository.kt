package com.ofppt.istak.data.repository

import com.ofppt.istak.data.model.StudentDetailsResponse
import com.ofppt.istak.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject

class StagiaireRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getStudentDetails(cef: String, groupeId: String? = null): Response<StudentDetailsResponse> {
        return apiService.getStudentDetails(cef, groupeId)
    }

    suspend fun getSchedule(week: String? = null, groupeId: String? = null): Response<com.ofppt.istak.data.model.ScheduleResponse> {
        return apiService.getSchedule(week, groupeId)
    }

    suspend fun updatePassword(request: com.ofppt.istak.data.model.ChangePasswordRequest) = apiService.updatePassword(request)
    suspend fun logout() = apiService.logout()
    suspend fun getNews() = apiService.getNews()
    suspend fun getExamSchedule(week: String? = null) = apiService.getExamSchedule(week)
    
    suspend fun debugHeaders() = apiService.debugHeaders()
    suspend fun hello(): Response<Map<String, Any>> = apiService.hello()
}
