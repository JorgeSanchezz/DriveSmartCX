package com.drivesmart.cx.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {
    private val kmFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

    fun formatKm(value: Double): String {
        return kmFormat.format(value)
    }
}
