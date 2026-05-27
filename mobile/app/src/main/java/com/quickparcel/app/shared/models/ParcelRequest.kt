package com.quickparcel.app.shared.models

data class ParcelRequest(
    val name: String,
    val description: String? = null,
    val weight: Double,
    val size: String,
    val category: String,
    val isFragile: Boolean = false
)