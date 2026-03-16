package com.ofppt.istak.data.model

data class Group(
    val id: String,
    val nom: String
)

data class GroupsResponse(
    val success: Boolean,
    val data: List<Group>
)

data class Seance(
    val id: Int,
    val start_time: String,
    val end_time: String,
    val module: String?
)

data class Student(
    val cef: String,
    val nom: String,
    val prenom: String,
    var isAbsent: Boolean = false,
    val status_message: String? = null,
    val status_color: String? = null,
    val is_blocked: Boolean = false,
    val absences: Map<String, Boolean>? = null
)

data class MarkAbsenceRequest(
    val date: String,
    val seance_id: Int,
    val absences: List<String>, // List of CEFs
    val groupe_id: String
)
