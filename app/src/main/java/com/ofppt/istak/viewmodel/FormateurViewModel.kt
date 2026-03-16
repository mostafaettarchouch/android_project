package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.Group
import com.ofppt.istak.data.model.MarkAbsenceRequest
import com.ofppt.istak.data.model.Seance
import com.ofppt.istak.data.model.Student
import com.ofppt.istak.data.repository.FormateurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FormateurViewModel @Inject constructor(
    private val repository: FormateurRepository,
    private val userDao: com.ofppt.istak.data.local.UserDao,
    private val tokenManager: com.ofppt.istak.data.local.TokenManager
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow<FormateurUiState>(FormateurUiState.Idle)
    val uiState: StateFlow<FormateurUiState> = _uiState
    
    val userName = userDao.getUser().map { it?.name ?: "Formateur" }

    // Selections
    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _selectedWeekStart = MutableStateFlow<LocalDate>(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    val selectedWeekStart: StateFlow<LocalDate> = _selectedWeekStart

    var selectedGroup: Group? = null
    var selectedSeance: Seance? = null
    
    // School Year Limits
    private var schoolYearStart: LocalDate? = null
    private var schoolYearEnd: LocalDate? = null

    // Lists
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _seances = MutableStateFlow<List<Seance>>(emptyList())
    val seances: StateFlow<List<Seance>> = _seances

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    init {
        fetchGroups()
        loadSchoolYear()
    }

    private fun loadSchoolYear() {
        viewModelScope.launch {
            val startStr = tokenManager.schoolYearStart.first()
            val endStr = tokenManager.schoolYearEnd.first()
            if (startStr != null && endStr != null) {
                try {
                    schoolYearStart = LocalDate.parse(startStr)
                    schoolYearEnd = LocalDate.parse(endStr)
                    
                    // Validate current selection
                    validateDateSelection()
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
        }
    }
    
    private fun validateDateSelection() {
        if (schoolYearStart != null && schoolYearEnd != null) {
            if (_selectedDate.value.isBefore(schoolYearStart)) {
                _selectedDate.value = schoolYearStart!!
                _selectedWeekStart.value = schoolYearStart!!.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            } else if (_selectedDate.value.isAfter(schoolYearEnd)) {
                _selectedDate.value = schoolYearEnd!!
                _selectedWeekStart.value = schoolYearEnd!!.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }
        }
    }

    fun fetchGroups() {
        viewModelScope.launch {
            try {
                val response = repository.getGroups()
                if (response.isSuccessful && response.body()?.success == true) {
                    _groups.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun onGroupSelected(group: Group) {
        selectedGroup = group
        fetchSeances()
        fetchStudents()
    }

    fun onDateSelected(date: LocalDate) {
        if (schoolYearStart != null && schoolYearEnd != null) {
            if (date.isBefore(schoolYearStart) || date.isAfter(schoolYearEnd)) {
                _uiState.value = FormateurUiState.Error("La date doit être comprise dans l'année scolaire active.")
                return
            }
        }
        _selectedDate.value = date
        if (selectedGroup != null) {
            fetchSeances()
            fetchStudents()
        }
    }

    fun onWeekSelected(weekStart: LocalDate) {
        // Allow navigation but maybe restrict if entirely out of bounds?
        // For now, let's just update
        _selectedWeekStart.value = weekStart
        
        // If the current selected date is not in this week, select the first valid day of this week
        val weekEnd = weekStart.plusDays(6)
        if (_selectedDate.value.isBefore(weekStart) || _selectedDate.value.isAfter(weekEnd)) {
             // Try to select Monday, or clamp to school year
             var newDate = weekStart
             if (schoolYearStart != null && newDate.isBefore(schoolYearStart)) {
                 newDate = schoolYearStart!!
             }
             if (schoolYearEnd != null && newDate.isAfter(schoolYearEnd)) {
                 newDate = schoolYearEnd!!
             }
             // Only update if within the week we just selected (otherwise we might jump back)
             if (newDate >= weekStart && newDate <= weekEnd) {
                 onDateSelected(newDate)
             } else {
                 // If the clamped date is outside the week, it means the whole week is outside.
                 // We can allow viewing the week but maybe disable actions?
                 // For now, just update selected date to week start to keep UI consistent
                 _selectedDate.value = weekStart
             }
        }
    }

    fun onSeanceSelected(seance: Seance) {
        selectedSeance = seance
        updateStudentAbsenceStatus()
    }

    private fun updateStudentAbsenceStatus() {
        if (selectedSeance == null) return
        
        val currentList = _students.value.map { student ->
            val isAbsent = student.absences?.get(selectedSeance!!.id.toString()) ?: false
            student.copy(isAbsent = isAbsent)
        }
        _students.value = currentList
    }

    fun fetchSeances() {
        viewModelScope.launch {
            if (selectedGroup != null) {
                try {
                    val response = repository.getSeances(selectedGroup!!.id, _selectedDate.value.toString())
                    if (response.isSuccessful) {
                        _seances.value = response.body() ?: emptyList()
                        // Reset selected seance when date changes
                        selectedSeance = null
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun fetchStudents() {
        viewModelScope.launch {
            if (selectedGroup != null) {
                try {
                    val response = repository.getStudents(selectedGroup!!.id, _selectedDate.value.toString())
                    if (response.isSuccessful) {
                        val studentsList = response.body() ?: emptyList()
                        _students.value = studentsList
                        // If seance is already selected (unlikely given flow, but possible), update status
                        if (selectedSeance != null) {
                            updateStudentAbsenceStatus()
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun toggleStudentAbsence(studentCef: String) {
        val currentList = _students.value.toMutableList()
        val index = currentList.indexOfFirst { it.cef == studentCef }
        if (index != -1) {
            val student = currentList[index]
            currentList[index] = student.copy(isAbsent = !student.isAbsent)
            _students.value = currentList
        }
    }

    fun submitAbsences() {
        if (selectedSeance == null || selectedGroup == null) return

        viewModelScope.launch {
            _uiState.value = FormateurUiState.Loading
            val absentStudents = _students.value.filter { it.isAbsent }.map { it.cef }
            
            val request = MarkAbsenceRequest(
                date = _selectedDate.value.toString(),
                seance_id = selectedSeance!!.id,
                absences = absentStudents,
                groupe_id = selectedGroup!!.id
            )

            try {
                val response = repository.markAbsence(request)
                if (response.isSuccessful) {
                    _uiState.value = FormateurUiState.Success("Absences enregistrées")
                } else {
                    _uiState.value = FormateurUiState.Error("Erreur lors de l'enregistrement")
                }
            } catch (e: Exception) {
                _uiState.value = FormateurUiState.Error("Erreur: ${e.message}")
            }
        }
    }
    
    fun getWeekLabel(start: LocalDate): String {
        val end = start.plusDays(5) // Saturday
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH)
        return "Semaine du ${start.format(formatter)} au ${end.format(formatter)}"
    }

    fun resetUiState() {
        _uiState.value = FormateurUiState.Idle
    }
}

sealed class FormateurUiState {
    object Idle : FormateurUiState()
    object Loading : FormateurUiState()
    data class Success(val message: String) : FormateurUiState()
    data class Error(val message: String) : FormateurUiState()
}
