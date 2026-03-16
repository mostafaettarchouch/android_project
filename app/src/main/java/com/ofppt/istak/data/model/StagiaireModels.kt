package com.ofppt.istak.data.model

data class StudentDetailsResponse(
    val success: Boolean,
    val data: StudentData
)

data class StudentData(
    val student: StudentInfo,
    val stats: StudentStats,
    val absences: List<Absence>,
    val groups: List<Group> = emptyList(),
    val sanctions: List<Sanction> = emptyList(),
    val authorizations: List<Authorization> = emptyList()
)

data class StudentInfo(
    val nom: String,
    val prenom: String,
    val groupe_nom: String,
    val cef: String,
    val is_authorized_to_enter: Boolean
)

data class StudentStats(
    val total_hours: Double,
    val total_justified: Double,
    val total_unjustified: Double
)

data class Absence(
    val date_absence: String,
    val seance_1: Boolean,
    val seance_2: Boolean,
    val seance_3: Boolean,
    val seance_4: Boolean,
    val motif: String?
)

data class Sanction(
    val id: Int,
    val type: String, // "Avertissement", "Blâme", "Exclusion"
    val start_date: String,
    val end_date: String,
    val motif: String?
)

data class Authorization(
    val id: Int,
    val start_date: String,
    val end_date: String,
    val motif: String?
)

data class ScheduleResponse(
    val success: Boolean,
    val data: ScheduleData
)

data class ScheduleData(
    val group_name: String,
    val group_id: String? = null,
    val week_start: String,
    val schedule: List<DaySchedule>,
    val available_weeks: List<WeekOption>? = null,
    val available_groups: List<GroupOption>? = null
)

data class GroupOption(
    val id: String,
    val nom: String
)

data class WeekOption(
    val label: String,
    val value: String,
    val start_date: String
)

data class DaySchedule(
    val day: String,
    val date: String,
    val sessions: List<Session>
)

data class Session(
    val creneau_id: Int,
    val creneau_label: String,
    val module: String?,
    val formateur: String?,
    val salle: String?,
    val is_exam: Boolean
)
