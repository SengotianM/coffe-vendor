package com.coffevendor.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Beverage(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @Json(name = "image_url") val imageUrl: String,
    val ingredients: List<String>,
    val category: BeverageCategory,
    @Json(name = "is_available") val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
enum class BeverageCategory {
    COFFEE,
    TEA,
    JUICE,
    SMOOTHIE,
    WATER,
    OTHER
}
