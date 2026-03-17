package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.local.TokenManager
import com.ofppt.istak.data.local.UserDao
import com.ofppt.istak.data.model.ChangePasswordRequest
import com.ofppt.istak.data.repository.FormateurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormateurProfileViewModel @Inject constructor(
    private val repository: FormateurRepository,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _passwordUpdateState = MutableStateFlow<PasswordUpdateState>(PasswordUpdateState.Idle)
    val passwordUpdateState: StateFlow<PasswordUpdateState> = _passwordUpdateState

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState

    val contactInfo = tokenManager.contactInfo
    val isDarkMode = tokenManager.isDarkMode

    init {
        loadProfile()
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            tokenManager.saveDarkMode(isDark)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = userDao.getUser().first()
            if (user != null) {
                _uiState.value = ProfileUiState.Success(user)
            } else {
                _uiState.value = ProfileUiState.Error("Utilisateur non trouvé")
            }
        }
    }

    fun updatePassword(current: String, new: String, confirm: String) {
        viewModelScope.launch {
            _passwordUpdateState.value = PasswordUpdateState.Loading
            try {
                val request = ChangePasswordRequest(current, new, confirm)
                val response = repository.updatePassword(request)
                if (response.isSuccessful) {
                    _passwordUpdateState.value = PasswordUpdateState.Success("Mot de passe mis à jour")
                } else {
                    _passwordUpdateState.value = PasswordUpdateState.Error("Erreur: ${response.code()}")
                }
            } catch (e: Exception) {
                _passwordUpdateState.value = PasswordUpdateState.Error("Erreur réseau: ${e.message}")
            }
        }
    }

    fun resetPasswordState() {
        _passwordUpdateState.value = PasswordUpdateState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            try {
                repository.logout()
                tokenManager.clearToken()
                userDao.clearUser()
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                tokenManager.clearToken()
                userDao.clearUser()
                _logoutState.value = LogoutState.Success
            }
        }
    }
}
