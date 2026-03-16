package com.ofppt.istak.data.remote

import com.ofppt.istak.data.model.LoginRequest
import com.ofppt.istak.data.model.LoginResponse
import com.ofppt.istak.data.model.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Map<String, Any>>

    @POST("user/password")
    suspend fun updatePassword(
        @Body request: ChangePasswordRequest
    ): Response<Map<String, Any>>

    // Stagiaire
    @GET("student/details")
    suspend fun getStudentDetails(
        @Query("cef") cef: String,
        @Query("groupe_id") groupeId: String? = null
    ): Response<com.ofppt.istak.data.model.StudentDetailsResponse>

    @GET("student/schedule")
    suspend fun getSchedule(
        @Query("week") week: String? = null,
        @Query("groupe_id") groupeId: String? = null
    ): Response<com.ofppt.istak.data.model.ScheduleResponse>

    @GET("absences/history")
    suspend fun getAbsencesHistory(
        @Query("cef") cef: String
    ): Response<Any>

    @GET("debug-headers")
    suspend fun debugHeaders(): Response<Map<String, Any>>

    @GET("hello")
    suspend fun hello(): Response<Map<String, Any>>

    @GET("news")
    suspend fun getNews(): Response<com.ofppt.istak.data.model.NewsResponse>

    @GET("student/exams")
    suspend fun getExamSchedule(
        @Query("week") week: String? = null
    ): Response<com.ofppt.istak.data.model.ScheduleResponse>

    // Formateur
    @GET("groups")
    suspend fun getGroups(): Response<com.ofppt.istak.data.model.GroupsResponse>

    @GET("seances")
    suspend fun getSeances(
        @Query("groupe_id") groupeId: String,
        @Query("date") date: String
    ): Response<List<com.ofppt.istak.data.model.Seance>>

    @GET("students")
    suspend fun getStudents(
        @Query("groupe_id") groupeId: String,
        @Query("date") date: String? = null
    ): Response<List<com.ofppt.istak.data.model.Student>>

    @POST("absences")
    suspend fun markAbsence(
        @Body request: com.ofppt.istak.data.model.MarkAbsenceRequest
    ): Response<Any>

    @GET("prof/schedule")
    suspend fun getProfSchedule(
        @Query("week") week: String? = null
    ): Response<com.ofppt.istak.data.model.ScheduleResponse>

    // Messaging
    @GET("messages")
    suspend fun getMessages(): Response<com.ofppt.istak.data.model.MessageResponse>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: com.ofppt.istak.data.model.SendMessageRequest
    ): Response<Any>

    @GET("messages/unread")
    suspend fun getUnreadCount(): Response<com.ofppt.istak.data.model.UnreadCountResponse>
}
