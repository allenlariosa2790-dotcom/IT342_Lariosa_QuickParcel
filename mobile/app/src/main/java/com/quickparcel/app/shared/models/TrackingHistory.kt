package com.quickparcel.app.shared.models

data class TrackingHistory(
    val id: Int,
    val delivery: Delivery? = null,
    val status: String,
    val location: String? = null,
    val timestamp: String,
    val notes: String? = null
)