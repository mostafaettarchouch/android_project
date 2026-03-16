package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.ScheduleData
import com.ofppt.istak.data.repository.FormateurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormateurScheduleViewModel @Inject constructor(
    private val repository: FormateurRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    private val _currentWeekStart = MutableStateFlow<java.time.LocalDate>(java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)))
    val currentWeekStart: StateFlow<java.time.LocalDate> = _currentWeekStart

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

                val response = repository.getProfSchedule(weekStr)
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

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        fetchSchedule()
    }

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
        fetchSchedule()
    }
}
