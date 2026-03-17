package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.User
import com.ofppt.istak.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: com.ofppt.istak.data.local.TokenManager,
    private val userDao: com.ofppt.istak.data.local.UserDao
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    val contactInfo = tokenManager.contactInfo

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!.user
                    val token = response.body()!!.token
                    android.util.Log.d("LoginViewModel", "Received User: $user")
                    tokenManager.saveToken(token)
                    tokenManager.saveUserId(user.id)
                    userDao.clearUser() // Clear old users
                    userDao.insertUser(user)
                    _loginState.value = LoginState.Success(user, token)
                } else {
                    _loginState.value = LoginState.Error("Identifiants incorrects")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Erreur de connexion: ${e.message}")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User, val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}
