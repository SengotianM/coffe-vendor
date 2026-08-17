package com.coffevendor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BeverageDao {

    @Query("SELECT * FROM beverages WHERE isAvailable = 1 ORDER BY category, name")
    fun getAvailableBeverages(): Flow<List<BeverageEntity>>

    @Query("SELECT * FROM beverages WHERE id = :id")
    suspend fun getBeverageById(id: String): BeverageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beverages: List<BeverageEntity>)

    @Query("DELETE FROM beverages")
    suspend fun deleteAll()
}

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE status != 'CANCELLED' AND status != 'SERVED' ORDER BY targetTime ASC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: String)
}
