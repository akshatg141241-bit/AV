package com.aryaleelastudios.astroveda.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gender: String = "Prefer not to say",
    val birthDate: String, // YYYY-MM-DD
    val birthTime: String, // HH:mm
    val isTimeUnknown: Boolean = false,
    val birthPlace: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String = "Asia/Kolkata",
    val maritalStatus: String = "Single",
    val occupation: String = "Other",
    val createdAt: Long = System.currentTimeMillis()
)
