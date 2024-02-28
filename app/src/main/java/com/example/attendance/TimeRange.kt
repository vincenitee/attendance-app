package com.example.attendance

import com.google.firebase.Timestamp

data class TimeRange(
    val timeStart: Timestamp?,
    val timeEnd: Timestamp?
)