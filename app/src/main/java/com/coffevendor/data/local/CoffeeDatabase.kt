package com.coffevendor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BeverageEntity::class, OrderEntity::class, UserEntity::class],
    version = 5,
    exportSchema = false
)
abstract class CoffeeDatabase : RoomDatabase() {
    abstract fun beverageDao(): BeverageDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
}
