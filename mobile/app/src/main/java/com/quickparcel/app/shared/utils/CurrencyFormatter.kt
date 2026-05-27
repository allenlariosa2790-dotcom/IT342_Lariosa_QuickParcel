package com.quickparcel.app.shared.utils

object CurrencyFormatter {
    fun format(amount: Double): String {
        return String.format("₱%,.2f", amount)
    }
}
