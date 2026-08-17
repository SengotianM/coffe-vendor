package com.coffevendor.data.model

data class Order(
    val id: String,
    val userId: String,
    val beverageId: String,
    val beverageName: String,
    val quantity: Int,
    val location: DeliveryLocation,
    val targetTime: String,
    val recurrence: RecurrenceType,
    val createdAt: String,
    val status: OrderStatus,
    val specialInstructions: String? = null
)

data class DeliveryLocation(
    val type: LocationType,
    val seatOrRow: String? = null,
    val hallName: String? = null
)

enum class LocationType {
    WORK_DESK,
    CONFERENCE_HALL
}

enum class RecurrenceType {
    NO_REPEAT,
    EVERY_1_HOUR,
    EVERY_2_HOURS,
    EVERY_3_HOURS
}

enum class OrderStatus {
    RECEIVED,
    PREPARING,
    OUT_FOR_DELIVERY,
    SERVED,
    CANCELLED
}
