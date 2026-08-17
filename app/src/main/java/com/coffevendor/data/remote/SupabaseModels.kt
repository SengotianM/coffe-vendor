package com.coffevendor.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    @SerialName("emp_id") val empId: String = "",
    @SerialName("seat_number") val seatNumber: String = "",
    @SerialName("mobile_number") val mobileNumber: String = "",
    val password: String = "",
    @SerialName("photo_uri") val photoUri: String? = null,
    @SerialName("favorite_beverages") val favoriteBeverages: String = "",
    @SerialName("is_biometric_enabled") val isBiometricEnabled: Boolean = false,
    val role: String = "CUSTOMER"
)

@Serializable
data class SupabaseBeverage(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String = "",
    val ingredients: String = "",
    val category: String = "",
    @SerialName("drawable_res") val drawableRes: String = "",
    @SerialName("has_sugar_option") val hasSugarOption: Boolean = true,
    @SerialName("is_available") val isAvailable: Boolean = true
)

@Serializable
data class SupabaseOrder(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("beverage_id") val beverageId: String = "",
    @SerialName("beverage_name") val beverageName: String = "",
    val quantity: Int = 1,
    @SerialName("location_type") val locationType: String = "",
    @SerialName("seat_or_row") val seatOrRow: String? = null,
    @SerialName("hall_name") val hallName: String? = null,
    @SerialName("target_time") val targetTime: String = "",
    val recurrence: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val status: String = "RECEIVED",
    @SerialName("special_instructions") val specialInstructions: String? = null
)
