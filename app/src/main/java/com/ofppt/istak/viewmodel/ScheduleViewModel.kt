package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.ScheduleData
import com.ofppt.istak.data.repository.StagiaireRepository
import com.ofppt.istak.data.local.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: StagiaireRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    private val _currentWeekStart = MutableStateFlow<java.time.LocalDate>(java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)))
    val currentWeekStart: StateFlow<java.time.LocalDate> = _currentWeekStart

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId

    init {
        fetchSchedule()
    }

    fun fetchSchedule() {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                // Format week as YYYY-Www
                val weekField = java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).weekOfWeekBasedYear()
                val date = _currentWeekStart.value
                val weekNum = date.get(weekField)
                val year = date.year
                // Ensure 2 digits for week
                val weekStr = String.format("%04d-W%02d", year, weekNum)
                
                val response = repository.getSchedule(weekStr, _selectedGroupId.value)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = ScheduleUiState.Success(response.body()!!.data)
                } else {
                    _uiState.value = ScheduleUiState.Error("Erreur: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erreur réseau: ${e.message}")
            }
        }
    }

    fun onGroupSelected(groupId: String) {
        _selectedGroupId.value = groupId
        fetchSchedule()
    }

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        fetchSchedule()
    }

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
        fetchSchedule()
    }


}

sealed class ScheduleUiState {
    object Loading : ScheduleUiState()
    data class Success(val data: ScheduleData) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}
