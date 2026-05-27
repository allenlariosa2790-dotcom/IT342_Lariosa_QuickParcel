package com.quickparcel.app.shared.models

data class Delivery(
    val id: Int,
    val trackingNumber: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val status: String,
    val estimatedCost: Double,
    val distance: Double? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val parcel: Parcel? = null,
    val rider: Rider? = null,
    val sender: Sender? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val deliveredTime: String? = null,
    val notes: String? = null
)

data class Parcel(
    val id: Int,
    val name: String,
    val description: String? = null,
    val weight: Double,
    val size: String,
    val category: String,
    val isFragile: Boolean = false,
    val sender: Sender? = null
)

data class Rider(
    val userId: Int,
    val user: User? = null
)

data class Sender(
    val userId: Int,
    val user: User? = null
)

data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val userType: String,
    val isActive: Boolean = true,
    val createdAt: String? = null
)