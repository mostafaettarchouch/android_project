package com.ofppt.istak.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String?,
    val role: String, // "admin", "formateur", "stagiaire"
    val stagiaire_cef: String? = null
)
