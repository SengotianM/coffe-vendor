package com.coffevendor.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class UserRole {
    CUSTOMER,
    VENDOR
}

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "emp_id") val empId: String,
    @Json(name = "seat_number") val seatNumber: String,
    @Json(name = "mobile_number") val mobileNumber: String,
    val password: String,
    @Json(name = "photo_uri") val photoUri: String? = null,
    @Json(name = "favorite_beverages") val favoriteBeverages: List<String> = emptyList(),
    @Json(name = "is_biometric_enabled") val isBiometricEnabled: Boolean = false,
    @Json(name = "is_logged_in") val isLoggedIn: Boolean = false,
    val role: UserRole = UserRole.CUSTOMER
)

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "emp_id") val empId: String,
    @Json(name = "seat_number") val seatNumber: String,
    @Json(name = "mobile_number") val mobileNumber: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "user_id") val userId: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class OtpRequest(
    @Json(name = "mobile_number") val mobileNumber: String
)

@JsonClass(generateAdapter = true)
data class OtpVerifyRequest(
    @Json(name = "mobile_number") val mobileNumber: String,
    val otp: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
