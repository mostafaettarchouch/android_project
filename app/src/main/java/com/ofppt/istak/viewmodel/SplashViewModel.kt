package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.local.TokenManager
import com.ofppt.istak.data.local.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val repository: com.ofppt.istak.data.repository.StagiaireRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    init {
        checkAppStatus()
    }

    private fun checkAppStatus() {
        viewModelScope.launch {
            try {
                // Check Maintenance Mode
                val helloResponse = repository.hello()
                if (helloResponse.isSuccessful) {
                    val body = helloResponse.body()
                    val isMaintenance = body?.get("maintenance_mode") as? Boolean ?: false
                    
                    if (isMaintenance) {
                        _startDestination.value = "maintenance"
                        return@launch
                    }

                    // Save School Year Info
                    val schoolYear = body?.get("school_year") as? Map<String, Any>
                    if (schoolYear != null) {
                        val start = schoolYear["start_date"] as? String
                        val end = schoolYear["end_date"] as? String
                        if (start != null && end != null) {
                            tokenManager.saveSchoolYear(start, end)
                        }
                    }
                    
                    // Save Contact Info
                    val contactInfo = body?.get("contact_info") as? Map<String, Any>
                    if (contactInfo != null) {
                        val phone = contactInfo["phone"] as? String
                        val email = contactInfo["email"] as? String
                        val facebook = contactInfo["facebook"] as? String
                        val instagram = contactInfo["instagram"] as? String
                        val whatsapp = contactInfo["whatsapp"] as? String
                        
                        tokenManager.saveContactInfo(phone, email, facebook, instagram, whatsapp)
                    }
                }
            } catch (e: Exception) {
                // If network fails, maybe proceed to login check (offline mode?) or show error?
                // For now, let's proceed to login check, assuming offline access might be allowed or handled later
            }

            checkLoginState()
        }
    }

    fun retry() {
        _startDestination.value = null
        checkAppStatus()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            val token = tokenManager.token.firstOrNull()
            val user = userDao.getUser().firstOrNull()

            if (!token.isNullOrEmpty() && user != null) {
                if (user.role == "stagiaire") {
                    _startDestination.value = "stagiaire_dashboard"
                } else if (user.role == "formateur") {
                    _startDestination.value = "formateur_dashboard"
                } else {
                    _startDestination.value = "login"
                }
            } else {
                _startDestination.value = "login"
            }
        }
    }
}
