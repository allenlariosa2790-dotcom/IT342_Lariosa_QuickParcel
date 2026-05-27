package com.quickparcel.app.shared.utils

object Constants {
    // API URLs
    const val BASE_URL = "http://192.168.1.2:8080/"
    const val API_VERSION = "v1"

    // Endpoints
    const val ENDPOINT_LOGIN = "api/auth/login"
    const val ENDPOINT_REGISTER = "api/auth/register"
    const val ENDPOINT_ME = "api/auth/me"
    const val ENDPOINT_DELIVERIES = "api/deliveries"
    const val ENDPOINT_MY_DELIVERIES = "api/deliveries/my"
    const val ENDPOINT_AVAILABLE_DELIVERIES = "api/deliveries/available"
    const val ENDPOINT_TRACKING = "api/deliveries/{id}/track"
    const val ENDPOINT_ACCEPT_DELIVERY = "api/deliveries/{id}/accept"
    const val ENDPOINT_UPDATE_STATUS = "api/deliveries/{id}/status"
    const val ENDPOINT_MARK_PAID = "api/deliveries/{id}/mark-paid"
    const val ENDPOINT_CALCULATE_DISTANCE = "api/deliveries/calculate-distance"
    const val ENDPOINT_UPLOAD_PARCEL_IMAGE = "api/upload/parcel/{parcelId}"
    const val ENDPOINT_PROFILE_PICTURE = "api/upload/profile-picture"

    // User Types
    const val USER_TYPE_SENDER = "SENDER"
    const val USER_TYPE_RIDER = "RIDER"
    const val USER_TYPE_ADMIN = "ADMIN"

    // Delivery Status
    const val STATUS_PENDING = "PENDING"
    const val STATUS_ACCEPTED = "ACCEPTED"
    const val STATUS_PICKED_UP = "PICKED_UP"
    const val STATUS_IN_TRANSIT = "IN_TRANSIT"
    const val STATUS_DELIVERED = "DELIVERED"
    const val STATUS_CANCELLED = "CANCELLED"

    // Payment Methods
    const val PAYMENT_COD = "COD"
    const val PAYMENT_STRIPE = "STRIPE"
    const val PAYMENT_PAYMONGO = "PAYMONGO_GCASH"

    // Payment Status
    const val PAYMENT_PENDING = "PENDING"
    const val PAYMENT_PAID = "PAID"
    const val PAYMENT_UNPAID = "UNPAID"

    // Parcel Sizes
    const val SIZE_SMALL = "SMALL"
    const val SIZE_MEDIUM = "MEDIUM"
    const val SIZE_LARGE = "LARGE"

    // Shared Preferences Keys
    const val PREF_TOKEN = "token"
    const val PREF_USER_DATA = "user_data"
    const val PREF_REMEMBER_ME = "remember_me"

    // Request Timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // File Upload Limits
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_PARCEL_IMAGE_SIZE_MB = 10

    // Map Defaults
    const val DEFAULT_LATITUDE = 10.3157
    const val DEFAULT_LONGITUDE = 123.8854
    const val DEFAULT_ZOOM = 13f
}

object Messages {
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_SERVER = "Server error. Please try again later."
    const val ERROR_UNAUTHORIZED = "Session expired. Please login again."
    const val ERROR_FORBIDDEN = "You don't have permission to access this."
    const val ERROR_NOT_FOUND = "Resource not found."
    const val ERROR_VALIDATION = "Please check your input and try again."

    const val SUCCESS_LOGIN = "Login successful!"
    const val SUCCESS_REGISTER = "Registration successful!"
    const val SUCCESS_LOGOUT = "Logged out successfully."
    const val SUCCESS_DELIVERY_CREATED = "Delivery created successfully!"
    const val SUCCESS_DELIVERY_ACCEPTED = "Delivery accepted!"
    const val SUCCESS_STATUS_UPDATED = "Status updated!"
    const val SUCCESS_PAYMENT_MARKED = "Payment marked as collected!"
    const val SUCCESS_PROFILE_UPDATED = "Profile updated successfully!"
    const val SUCCESS_PASSWORD_CHANGED = "Password changed successfully!"
    const val SUCCESS_IMAGE_UPLOADED = "Image uploaded successfully!"

    const val CONFIRM_CANCEL_DELIVERY = "Are you sure you want to cancel this delivery?"
    const val CONFIRM_DELETE_ACCOUNT = "Are you sure you want to delete your account? This action cannot be undone."
    const val CONFIRM_LOGOUT = "Are you sure you want to logout?"
}

object ValidationMessages {
    const val REQUIRED_FIELD = "This field is required"
    const val INVALID_EMAIL = "Please enter a valid email address"
    const val PASSWORD_TOO_SHORT = "Password must be at least 6 characters"
    const val PASSWORD_MISMATCH = "Passwords do not match"
    const val INVALID_PHONE = "Please enter a valid phone number"
    const val INVALID_WEIGHT = "Please enter a valid weight"
    const val INVALID_DISTANCE = "Could not calculate distance. Please check addresses."
}