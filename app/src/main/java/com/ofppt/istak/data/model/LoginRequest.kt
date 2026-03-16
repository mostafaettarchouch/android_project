package com.ofppt.istak.data.model

data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String,
    val new_password_confirmation: String
)
