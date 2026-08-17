package com.coffevendor.data.model

data class CreateOrderRequest(
    val beverageId: String,
    val quantity: Int,
    val location: DeliveryLocation,
    val targetTime: String,
    val recurrence: RecurrenceType,
    val specialInstructions: String? = null
)
