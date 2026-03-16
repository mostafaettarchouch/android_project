package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.ScheduleData
import com.ofppt.istak.data.repository.StagiaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamScheduleViewModel @Inject constructor(
    private val repository: StagiaireRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    private var availableWeeks: List<com.ofppt.istak.data.model.WeekOption> = emptyList()
    private var currentWeekIndex: Int = -1

    init {
        loadSchedule()
    }

    fun loadSchedule(week: String? = null) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                val response = repository.getExamSchedule(week)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data
                    _uiState.value = ScheduleUiState.Success(data)
                    
                    // Update available weeks
                    if (!data.available_weeks.isNullOrEmpty()) {
                        availableWeeks = data.available_weeks
                        // Find current week index based on returned week_start or just the week param
                        // The API returns week_start (YYYY-MM-DD). available_weeks has start_date.
                        currentWeekIndex = availableWeeks.indexOfFirst { it.start_date == data.week_start }
                    }
                } else {
                    _uiState.value = ScheduleUiState.Error("Erreur de chargement du calendrier des examens")
                }
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Erreur réseau: ${e.message}")
            }
        }
    }

    fun nextWeek() {
        if (availableWeeks.isNotEmpty() && currentWeekIndex < availableWeeks.size - 1) {
            val nextWeek = availableWeeks[currentWeekIndex + 1]
            loadSchedule(nextWeek.value)
        }
    }

    fun previousWeek() {
        if (availableWeeks.isNotEmpty() && currentWeekIndex > 0) {
            val prevWeek = availableWeeks[currentWeekIndex - 1]
            loadSchedule(prevWeek.value)
        }
    }

    fun hasNextWeek(): Boolean {
        return availableWeeks.isNotEmpty() && currentWeekIndex < availableWeeks.size - 1
    }

    fun hasPreviousWeek(): Boolean {
        return availableWeeks.isNotEmpty() && currentWeekIndex > 0
    }
}
