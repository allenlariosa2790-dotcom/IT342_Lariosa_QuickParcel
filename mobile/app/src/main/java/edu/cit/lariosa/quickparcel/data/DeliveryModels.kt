package edu.cit.lariosa.quickparcel.data

// Sender Dashboard Models
data class ActiveDelivery(
        val trackingNumber: String,
        val status: String,
        val pickup: String,
        val dropoff: String,
        val rider: String,
        val eta: String
)

data class RecentDelivery(
        val trackingNumber: String,
        val address: String,
        val date: String,
        val eta: String
)

// Rider Dashboard Models
data class AvailableDelivery(
        val trackingNumber: String,
        val pickup: String,
        val dropoff: String,
        val earnings: String,
        val distance: String
)