package com.coffevendor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByUserId(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobileNumber = :mobileNumber LIMIT 1")
    suspend fun getUserByMobile(mobileNumber: String): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = :isLoggedIn WHERE id = :id")
    suspend fun updateLoginStatus(id: String, isLoggedIn: Boolean)

    @Query("UPDATE users SET isBiometricEnabled = :enabled WHERE id = :id")
    suspend fun updateBiometricStatus(id: String, enabled: Boolean)

    @Query("UPDATE users SET photoUri = :photoUri WHERE id = :id")
    suspend fun updatePhoto(id: String, photoUri: String)

    @Query("UPDATE users SET favoriteBeverages = :favorites WHERE id = :id")
    suspend fun updateFavorites(id: String, favorites: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
