package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.StudentData
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
class StagiaireViewModel @Inject constructor(
    private val repository: StagiaireRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<StagiaireUiState>(StagiaireUiState.Loading)
    val uiState: StateFlow<StagiaireUiState> = _uiState

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData(groupId: String? = null) {
        viewModelScope.launch {
            _uiState.value = StagiaireUiState.Loading
            try {
                // Wait for user to be saved
                val user = userDao.getUser().filterNotNull().first()
                
                if (user.stagiaire_cef != null) {
                    val response = repository.getStudentDetails(user.stagiaire_cef, groupId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        _uiState.value = StagiaireUiState.Success(response.body()!!.data)
                    } else {
                        var errorMsg = "Erreur: ${response.code()}"
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            errorMsg += "\nBody: $errorBody"
                        }
                        _uiState.value = StagiaireUiState.Error(errorMsg)
                    }
                } else {
                    _uiState.value = StagiaireUiState.Error("Err: Role=${user.role}, CEF=${user.stagiaire_cef}")
                }
            } catch (e: Exception) {
                _uiState.value = StagiaireUiState.Error("Erreur réseau: ${e.message}")
            }
        }
    }

    fun switchGroup(groupId: String) {
        fetchDashboardData(groupId)
    }
}

sealed class StagiaireUiState {
    object Loading : StagiaireUiState()
    data class Success(val data: StudentData) : StagiaireUiState()
    data class Error(val message: String) : StagiaireUiState()
}
