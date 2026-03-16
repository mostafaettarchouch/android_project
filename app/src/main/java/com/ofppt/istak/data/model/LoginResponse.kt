package com.ofppt.istak.data.model

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: User
)
