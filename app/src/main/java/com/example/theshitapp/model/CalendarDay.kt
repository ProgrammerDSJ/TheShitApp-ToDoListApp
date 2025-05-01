package com.example.theshitapp.model

import java.util.Date

data class CalendarDay(
    val date: Date?,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val hasTask: Boolean = false
) 