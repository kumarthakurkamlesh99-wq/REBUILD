package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SchoolState {
    HOME,
    TRAVELLING_TO_SCHOOL,
    IN_SCHOOL,
    TRAVELLING_HOME,
    ARRIVED_HOME
}

@Entity(tableName = "school_status_logs")
data class SchoolStatusEntity(
    @PrimaryKey
    val date: String, // format: "yyyy-MM-dd"
    val currentState: SchoolState = SchoolState.HOME,
    val dispatchSchoolTime: Long? = null,
    val arrivedSchoolTime: Long? = null,
    val dispatchHomeTime: Long? = null,
    val arrivedHomeTime: Long? = null,
    val travelToSchoolMinutes: Int = 0,
    val travelHomeMinutes: Int = 0,
    val inSchoolMinutes: Int = 0,
    val isPresent: Boolean = false,
    val isHoliday: Boolean = false,
    val notes: String = ""
)
