package com.coffevendor.data.local

import androidx.room.*
import com.coffevendor.data.model.User
import com.coffevendor.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val empId: String,
    val seatNumber: String,
    val mobileNumber: String,
    val password: String,
    val photoUri: String?,
    val favoriteBeverages: String,
    val isBiometricEnabled: Boolean,
    val isLoggedIn: Boolean,
    val role: String,
    val accessToken: String = "",
    val refreshToken: String = "",
    val accessTokenExpiry: Long = 0L,
    val refreshTokenExpiry: Long = 0L
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    userId = userId,
    username = username,
    empId = empId,
    seatNumber = seatNumber,
    mobileNumber = mobileNumber,
    password = password,
    photoUri = photoUri,
    favoriteBeverages = favoriteBeverages.joinToString(","),
    isBiometricEnabled = isBiometricEnabled,
    isLoggedIn = isLoggedIn,
    role = role.name,
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessTokenExpiry = accessTokenExpiry,
    refreshTokenExpiry = refreshTokenExpiry
)

fun UserEntity.toDomain(): User = User(
    id = id,
    userId = userId,
    username = username,
    empId = empId,
    seatNumber = seatNumber,
    mobileNumber = mobileNumber,
    password = password,
    photoUri = photoUri,
    favoriteBeverages = favoriteBeverages.split(",").filter { it.isNotBlank() },
    isBiometricEnabled = isBiometricEnabled,
    isLoggedIn = isLoggedIn,
    role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.CUSTOMER },
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessTokenExpiry = accessTokenExpiry,
    refreshTokenExpiry = refreshTokenExpiry
)
