package com.coffevendor.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "beverage_id") val beverageId: String,
    val quantity: Int,
    val location: DeliveryLocation,
    @Json(name = "target_time") val targetTime: String,
    val recurrence: RecurrenceType,
    @Json(name = "special_instructions") val specialInstructions: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderResponse(
    val success: Boolean,
    val order: Order?,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class BeverageListResponse(
    val success: Boolean,
    val beverages: List<Beverage>
)

@JsonClass(generateAdapter = true)
data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

@JsonClass(generateAdapter = true)
data class Boardroom(
    val id: String,
    val name: String,
    val floor: Int,
    val capacity: Int
)

@JsonClass(generateAdapter = true)
data class BoardroomListResponse(
    val success: Boolean,
    val boardrooms: List<Boardroom>
)
