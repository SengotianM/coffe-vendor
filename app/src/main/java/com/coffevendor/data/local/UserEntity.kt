package com.coffevendor.data.local

import androidx.room.*
import com.coffevendor.data.model.User

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
    val isLoggedIn: Boolean
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
    isLoggedIn = isLoggedIn
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
    isLoggedIn = isLoggedIn
)
