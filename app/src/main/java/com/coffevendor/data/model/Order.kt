package com.coffevendor.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Order(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "beverage_id") val beverageId: String,
    @Json(name = "beverage_name") val beverageName: String,
    val quantity: Int,
    val location: DeliveryLocation,
    @Json(name = "target_time") val targetTime: String,
    val recurrence: RecurrenceType,
    @Json(name = "created_at") val createdAt: String,
    val status: OrderStatus,
    @Json(name = "special_instructions") val specialInstructions: String? = null
)

@JsonClass(generateAdapter = true)
data class DeliveryLocation(
    val type: LocationType,
    @Json(name = "seat_or_row") val seatOrRow: String? = null,
    @Json(name = "hall_name") val hallName: String? = null
)

@JsonClass(generateAdapter = true)
enum class LocationType {
    @Json(name = "work_desk") WORK_DESK,
    @Json(name = "conference_hall") CONFERENCE_HALL
}

@JsonClass(generateAdapter = true)
enum class RecurrenceType {
    @Json(name = "no_repeat") NO_REPEAT,
    @Json(name = "every_1_hour") EVERY_1_HOUR,
    @Json(name = "every_2_hours") EVERY_2_HOURS,
    @Json(name = "every_3_hours") EVERY_3_HOURS
}

@JsonClass(generateAdapter = true)
enum class OrderStatus {
    @Json(name = "received") RECEIVED,
    @Json(name = "preparing") PREPARING,
    @Json(name = "out_for_delivery") OUT_FOR_DELIVERY,
    @Json(name = "served") SERVED,
    @Json(name = "cancelled") CANCELLED
}
