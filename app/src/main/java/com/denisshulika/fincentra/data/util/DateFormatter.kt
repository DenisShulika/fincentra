package com.denisshulika.fincentra.data.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    private fun getFormatter(pattern: String): DateTimeFormatter {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    fun formatFullDate(timestamp: Long): String =
        getFormatter("dd.MM.yyyy").format(Instant.ofEpochMilli(timestamp))

    fun formatDateTime(timestamp: Long): String =
        getFormatter("dd.MM, HH:mm").format(Instant.ofEpochMilli(timestamp))

    fun formatDayMonth(timestamp: Long): String =
        getFormatter("dd MMM").format(Instant.ofEpochMilli(timestamp))

    fun formatMonthYear(timestamp: Long): String =
        getFormatter("MMMM yyyy").format(Instant.ofEpochMilli(timestamp))

    fun formatTimeOnly(timestamp: Long): String =
        getFormatter("HH:mm").format(Instant.ofEpochMilli(timestamp))
}