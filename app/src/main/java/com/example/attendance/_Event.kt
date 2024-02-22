package com.example.attendance
import com.google.firebase.Timestamp

data class Event (
    val name: String?,
    val timeInStart: Timestamp?,
    val timeInEnd: Timestamp?,
    val timeOutStart: Timestamp?,
    val timeOutEnd: Timestamp?
)