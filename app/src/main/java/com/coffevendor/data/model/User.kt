package com.coffevendor.data.model

enum class UserRole {
    CUSTOMER,
    VENDOR
}

data class User(
    val id: String,
    val userId: String,
    val username: String,
    val empId: String,
    val seatNumber: String,
    val mobileNumber: String,
    val password: String,
    val photoUri: String? = null,
    val favoriteBeverages: List<String> = emptyList(),
    val isBiometricEnabled: Boolean = false,
    val isLoggedIn: Boolean = false,
    val role: UserRole = UserRole.CUSTOMER,
    val accessToken: String = "",
    val refreshToken: String = "",
    val accessTokenExpiry: Long = 0L,
    val refreshTokenExpiry: Long = 0L
)

data class SignUpRequest(
    val userId: String,
    val username: String,
    val empId: String,
    val seatNumber: String,
    val mobileNumber: String,
    val password: String
)

data class LoginRequest(
    val userId: String,
    val password: String
)

data class OtpRequest(
    val mobileNumber: String
)

data class OtpVerifyRequest(
    val mobileNumber: String,
    val otp: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
