package com.ofppt.istak.data.repository

import com.ofppt.istak.data.model.Group
import com.ofppt.istak.data.model.MarkAbsenceRequest
import com.ofppt.istak.data.model.Seance
import com.ofppt.istak.data.model.Student
import com.ofppt.istak.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject

class FormateurRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getGroups(): Response<com.ofppt.istak.data.model.GroupsResponse> {
        return apiService.getGroups()
    }

    suspend fun getSeances(groupeId: String, date: String): Response<List<Seance>> {
        return apiService.getSeances(groupeId, date)
    }

    suspend fun getStudents(groupeId: String, date: String? = null): Response<List<Student>> {
        return apiService.getStudents(groupeId, date)
    }

    suspend fun markAbsence(request: MarkAbsenceRequest): Response<Any> {
        return apiService.markAbsence(request)
    }

    suspend fun getProfSchedule(week: String? = null): Response<com.ofppt.istak.data.model.ScheduleResponse> {
        return apiService.getProfSchedule(week)
    }

    suspend fun updatePassword(request: com.ofppt.istak.data.model.ChangePasswordRequest): Response<Map<String, Any>> {
        return apiService.updatePassword(request)
    }

    suspend fun logout(): Response<Map<String, Any>> {
        return apiService.logout()
    }
}
