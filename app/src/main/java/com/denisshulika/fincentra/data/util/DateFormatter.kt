package com.denisshulika.fincentra.data.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    val fullDate: SimpleDateFormat get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateTime: SimpleDateFormat get() = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault())
    val dayMonth: SimpleDateFormat get() = SimpleDateFormat("dd MMM", Locale.getDefault())
    val monthYear: SimpleDateFormat get() = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
}