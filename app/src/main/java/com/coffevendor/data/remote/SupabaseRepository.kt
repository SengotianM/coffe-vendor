package com.coffevendor.data.remote

import com.coffevendor.data.local.BeverageDao
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.local.toDomain
import com.coffevendor.data.local.toEntity
import com.coffevendor.data.model.Beverage
import com.coffevendor.data.model.BeverageCategory
import com.coffevendor.data.model.DeliveryLocation
import com.coffevendor.data.model.LocationType
import com.coffevendor.data.model.Order
import com.coffevendor.data.model.OrderStatus
import com.coffevendor.data.model.RecurrenceType
import com.coffevendor.data.model.User
import com.coffevendor.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRepository @Inject constructor(
    private val beverageDao: BeverageDao,
    private val orderDao: OrderDao,
    private val userDao: UserDao
) {
    private val client = SupabaseClient

    // ── Users ──

    suspend fun login(userId: String, password: String): User? = withContext(Dispatchers.IO) {
        try {
            val json = client.get("users", "user_id=eq.$userId")
            val arr = JSONArray(json)
            if (arr.length() == 0) {
                val local = userDao.getUserByUserId(userId)
                if (local != null && local.password == password) {
                    return@withContext local.toDomain()
                }
                return@withContext null
            }
            val obj = arr.getJSONObject(0)
            val remotePw = obj.optString("password", "")
            val remoteRole = obj.optString("role", "CUSTOMER")
            if (remotePw == password) {
                User(
                    id = obj.optString("id", userId),
                    userId = userId,
                    username = obj.optString("username", userId),
                    empId = obj.optString("emp_id", ""),
                    seatNumber = obj.optString("seat_number", ""),
                    mobileNumber = obj.optString("mobile_number", ""),
                    password = remotePw,
                    role = try { UserRole.valueOf(remoteRole) } catch (_: Exception) { UserRole.CUSTOMER }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            val local = userDao.getUserByUserId(userId)
            if (local != null && local.password == password) {
                local.toDomain()
            } else {
                null
            }
        }
    }

    suspend fun signUp(userId: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("user_id", userId)
                put("password", password)
                put("role", "CUSTOMER")
            }
            client.insert("users", body.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Beverages ──

    suspend fun syncBeveragesFromRemote(): List<Beverage> = withContext(Dispatchers.IO) {
        try {
            val json = client.get("beverages", "is_available=eq.true")
            val arr = JSONArray(json)
            val list = mutableListOf<Beverage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Beverage(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        price = obj.getDouble("price"),
                        imageUrl = obj.optString("image_url", ""),
                        ingredients = obj.optString("ingredients", "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        category = try { BeverageCategory.valueOf(obj.optString("category", "OTHER")) } catch (_: Exception) { BeverageCategory.OTHER },
                        drawableRes = obj.optString("drawable_res", "ic_beverage_coffee"),
                        isAvailable = obj.optBoolean("is_available", true)
                    )
                )
            }
            beverageDao.deleteAll()
            beverageDao.insertAll(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            beverageDao.getAllBeverages().first().map { it.toDomain() }
        }
    }

    suspend fun pushBeverage(beverage: Beverage) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("id", beverage.id)
                put("name", beverage.name)
                put("description", beverage.description)
                put("price", beverage.price)
                put("category", beverage.category.name)
                put("is_available", beverage.isAvailable)
                put("image_url", beverage.imageUrl)
                put("ingredients", beverage.ingredients.joinToString(","))
                put("drawable_res", beverage.drawableRes)
            }
            client.insert("beverages", body.toString())
        } catch (_: Exception) {}
    }

    suspend fun deleteBeverageRemote(id: String) = withContext(Dispatchers.IO) {
        try {
            client.delete("beverages", "id=eq.$id")
        } catch (_: Exception) {}
    }

    suspend fun toggleBeverageAvailabilityRemote(id: String, available: Boolean) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("is_available", available) }
            client.update("beverages", body.toString(), "id=eq.$id")
        } catch (_: Exception) {}
    }

    // ── Orders ──

    suspend fun pushOrder(order: Order) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("id", order.id)
                put("user_id", order.userId)
                put("beverage_id", order.beverageId)
                put("beverage_name", order.beverageName)
                put("quantity", order.quantity)
                put("location_type", order.location.type.name)
                put("seat_or_row", order.location.seatOrRow ?: JSONObject.NULL)
                put("hall_name", order.location.hallName ?: JSONObject.NULL)
                put("target_time", order.targetTime)
                put("recurrence", order.recurrence.name)
                put("created_at", order.createdAt)
                put("status", order.status.name)
                put("special_instructions", order.specialInstructions ?: JSONObject.NULL)
            }
            client.insert("orders", body.toString())
        } catch (e: Exception) {
            orderDao.insert(order.toEntity())
        }
    }

    suspend fun syncOrdersFromRemote(): List<Order> = withContext(Dispatchers.IO) {
        try {
            val json = client.get("orders")
            val arr = JSONArray(json)
            val list = mutableListOf<Order>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Order(
                        id = obj.getString("id"),
                        userId = obj.getString("user_id"),
                        beverageId = obj.getString("beverage_id"),
                        beverageName = obj.optString("beverage_name", ""),
                        quantity = obj.getInt("quantity"),
                        location = DeliveryLocation(
                            type = try { LocationType.valueOf(obj.optString("location_type", "WORK_DESK")) } catch (_: Exception) { LocationType.WORK_DESK },
                            seatOrRow = obj.optString("seat_or_row", null),
                            hallName = obj.optString("hall_name", null)
                        ),
                        targetTime = obj.optString("target_time", ""),
                        recurrence = try { RecurrenceType.valueOf(obj.optString("recurrence", "NO_REPEAT")) } catch (_: Exception) { RecurrenceType.NO_REPEAT },
                        createdAt = obj.optString("created_at", ""),
                        status = try { OrderStatus.valueOf(obj.getString("status")) } catch (_: Exception) { OrderStatus.RECEIVED },
                        specialInstructions = obj.optString("special_instructions", null)
                    )
                )
            }
            list
        } catch (e: Exception) {
            orderDao.getAllOrders().first().map { it.toDomain() }
        }
    }

    suspend fun updateOrderStatusRemote(orderId: String, status: OrderStatus) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("status", status.name) }
            client.update("orders", body.toString(), "id=eq.$orderId")
        } catch (_: Exception) {}
    }
}
