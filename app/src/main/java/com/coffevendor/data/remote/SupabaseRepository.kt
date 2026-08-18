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
import com.coffevendor.util.JwtManager
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

    // ── Auth helpers ──

    suspend fun activateUserToken(userId: String) {
        val entity = userDao.getUserByUserId(userId) ?: return
        if (entity.accessToken.isNotBlank() && JwtManager.validateAccessToken(entity.accessToken)) {
            client.setActiveToken(entity.accessToken)
        } else if (entity.refreshToken.isNotBlank() && JwtManager.isRefreshTokenValid(entity.refreshToken, entity.refreshTokenExpiry)) {
            val refreshed = JwtManager.refreshAccessToken(entity.refreshToken, entity.userId, entity.refreshTokenExpiry) ?: return
            userDao.updateTokens(userId, refreshed.accessToken, refreshed.accessTokenExpiry)
            client.setActiveToken(refreshed.accessToken)
        } else {
            val tokens = JwtManager.generateTokenPair(userId)
            userDao.updateTokens(userId, tokens.accessToken, tokens.accessTokenExpiry)
            try {
                val body = JSONObject().apply {
                    put("access_token", tokens.accessToken)
                    put("refresh_token", tokens.refreshToken)
                    put("access_token_expiry", tokens.accessTokenExpiry)
                    put("refresh_token_expiry", tokens.refreshTokenExpiry)
                }
                client.update("users", body.toString(), "user_id=eq.$userId")
            } catch (_: Exception) {}
            client.setActiveToken(tokens.accessToken)
        }
    }

    private suspend fun refreshIfNeeded(userId: String): Boolean {
        val entity = userDao.getUserByUserId(userId) ?: return false
        if (JwtManager.validateAccessToken(entity.accessToken)) {
            client.setActiveToken(entity.accessToken)
            return true
        }
        if (entity.refreshToken.isNotBlank() && JwtManager.isRefreshTokenValid(entity.refreshToken, entity.refreshTokenExpiry)) {
            val refreshed = JwtManager.refreshAccessToken(entity.refreshToken, userId, entity.refreshTokenExpiry) ?: return false
            userDao.updateTokens(userId, refreshed.accessToken, refreshed.accessTokenExpiry)
            try {
                val body = JSONObject().apply {
                    put("access_token", refreshed.accessToken)
                    put("access_token_expiry", refreshed.accessTokenExpiry)
                }
                client.update("users", body.toString(), "user_id=eq.$userId")
            } catch (_: Exception) {}
            client.setActiveToken(refreshed.accessToken)
            return true
        }
        return false
    }

    fun validateCurrentToken(): Boolean {
        val entity = runCatching {
            kotlinx.coroutines.runBlocking { userDao.getLoggedInUser() }
        }.getOrNull() ?: return false
        return JwtManager.validateAccessToken(entity.accessToken)
    }

    // ── Users ──

    suspend fun login(userId: String, password: String): User? = withContext(Dispatchers.IO) {
        try {
            val json = client.get("users", "user_id=eq.$userId")
            val arr = JSONArray(json)
            if (arr.length() == 0) {
                val local = userDao.getUserByUserId(userId)
                if (local != null && local.password == password) {
                    val tokens = JwtManager.generateTokenPair(userId)
                    userDao.updateTokens(userId, tokens.accessToken, tokens.accessTokenExpiry)
                    client.setActiveToken(tokens.accessToken)
                    return@withContext local.toDomain().copy(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        accessTokenExpiry = tokens.accessTokenExpiry,
                        refreshTokenExpiry = tokens.refreshTokenExpiry
                    )
                }
                return@withContext null
            }
            val obj = arr.getJSONObject(0)
            val remotePw = obj.optString("password", "")
            val remoteRole = obj.optString("role", "CUSTOMER")
            if (remotePw == password) {
                val tokens = JwtManager.generateTokenPair(userId)
                val user = User(
                    id = obj.optString("id", userId),
                    userId = userId,
                    username = obj.optString("username", userId),
                    empId = obj.optString("emp_id", ""),
                    seatNumber = obj.optString("seat_number", ""),
                    mobileNumber = obj.optString("mobile_number", ""),
                    password = remotePw,
                    role = try { UserRole.valueOf(remoteRole) } catch (_: Exception) { UserRole.CUSTOMER },
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    accessTokenExpiry = tokens.accessTokenExpiry,
                    refreshTokenExpiry = tokens.refreshTokenExpiry
                )
                userDao.insert(user.toEntity())
                try {
                    val body = JSONObject().apply {
                        put("access_token", tokens.accessToken)
                        put("refresh_token", tokens.refreshToken)
                        put("access_token_expiry", tokens.accessTokenExpiry)
                        put("refresh_token_expiry", tokens.refreshTokenExpiry)
                        put("is_logged_in", true)
                    }
                    client.update("users", body.toString(), "user_id=eq.$userId")
                } catch (_: Exception) {}
                client.setActiveToken(tokens.accessToken)
                user
            } else {
                null
            }
        } catch (e: Exception) {
            val local = userDao.getUserByUserId(userId)
            if (local != null && local.password == password) {
                val tokens = JwtManager.generateTokenPair(userId)
                userDao.updateTokens(userId, tokens.accessToken, tokens.accessTokenExpiry)
                client.setActiveToken(tokens.accessToken)
                local.toDomain().copy(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    accessTokenExpiry = tokens.accessTokenExpiry,
                    refreshTokenExpiry = tokens.refreshTokenExpiry
                )
            } else {
                null
            }
        }
    }

    suspend fun signUp(
        userId: String,
        username: String,
        empId: String,
        seatNumber: String,
        mobileNumber: String,
        password: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val tokens = JwtManager.generateTokenPair(userId)
            val body = JSONObject().apply {
                put("id", userId)
                put("user_id", userId)
                put("username", username)
                put("emp_id", empId)
                put("seat_number", seatNumber)
                put("mobile_number", mobileNumber)
                put("password", password)
                put("role", "CUSTOMER")
                put("favorite_beverages", "")
                put("is_biometric_enabled", false)
                put("is_logged_in", false)
                put("access_token", tokens.accessToken)
                put("refresh_token", tokens.refreshToken)
                put("access_token_expiry", tokens.accessTokenExpiry)
                put("refresh_token_expiry", tokens.refreshTokenExpiry)
            }
            client.insert("users", body.toString())

            val user = User(
                id = userId,
                userId = userId,
                username = username,
                empId = empId,
                seatNumber = seatNumber,
                mobileNumber = mobileNumber,
                password = password,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                accessTokenExpiry = tokens.accessTokenExpiry,
                refreshTokenExpiry = tokens.refreshTokenExpiry
            )
            userDao.insert(user.toEntity())
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Beverages ──

    suspend fun updateBiometricStatus(userId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        try { refreshIfNeeded(userId) } catch (_: Exception) {}
        try {
            val body = JSONObject().apply { put("is_biometric_enabled", enabled) }
            client.update("users", body.toString(), "user_id=eq.$userId")
        } catch (_: Exception) {}
    }

    suspend fun updateFavorites(userId: String, favorites: List<String>) = withContext(Dispatchers.IO) {
        try { refreshIfNeeded(userId) } catch (_: Exception) {}
        try {
            val body = JSONObject().apply { put("favorite_beverages", favorites.joinToString(",")) }
            client.update("users", body.toString(), "user_id=eq.$userId")
        } catch (_: Exception) {}
    }

    suspend fun updatePhoto(userId: String, photoUri: String?) = withContext(Dispatchers.IO) {
        try { refreshIfNeeded(userId) } catch (_: Exception) {}
        try {
            val body = JSONObject().apply { put("photo_uri", photoUri ?: JSONObject.NULL) }
            client.update("users", body.toString(), "user_id=eq.$userId")
        } catch (_: Exception) {}
    }

    suspend fun updateLoginStatus(userId: String, loggedIn: Boolean) = withContext(Dispatchers.IO) {
        try { refreshIfNeeded(userId) } catch (_: Exception) {}
        try {
            val body = JSONObject().apply { put("is_logged_in", loggedIn) }
            client.update("users", body.toString(), "user_id=eq.$userId")
        } catch (_: Exception) {}
    }

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
            orderDao.insert(order.toEntity())
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

    // ── Logout ──

    suspend fun clearTokens(userId: String) = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("access_token", "")
                put("refresh_token", "")
                put("access_token_expiry", 0)
                put("refresh_token_expiry", 0)
                put("is_logged_in", false)
            }
            client.update("users", body.toString(), "user_id=eq.$userId")
        } catch (_: Exception) {}
        client.setActiveToken("")
    }
}
