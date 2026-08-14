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
    @Json(name = "drawable_res") val drawableRes: String,
    val hasSugarOption: Boolean = true,
    @Json(name = "is_available") val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
enum class BeverageCategory {
    COFFEE,
    TEA,
    MILK,
    HEALTH_DRINK,
    OTHER
}

enum class SugarOption {
    WITH_SUGAR,
    WITHOUT_SUGAR
}

object BeverageData {
    val beverages = listOf(
        Beverage(
            id = "1",
            name = "Tea",
            description = "Classic Indian chai tea",
            price = 15.0,
            imageUrl = "",
            ingredients = listOf("Tea leaves", "Milk", "Water", "Sugar"),
            category = BeverageCategory.TEA,
            drawableRes = "ic_beverage_coffee",
            hasSugarOption = true
        ),
        Beverage(
            id = "2",
            name = "Green Tea",
            description = "Refreshing green tea",
            price = 20.0,
            imageUrl = "",
            ingredients = listOf("Green tea leaves", "Water"),
            category = BeverageCategory.TEA,
            drawableRes = "ic_beverage_green_tea",
            hasSugarOption = true
        ),
        Beverage(
            id = "3",
            name = "Badam Milk",
            description = "Rich almond milk drink",
            price = 30.0,
            imageUrl = "",
            ingredients = listOf("Almonds", "Milk", "Sugar", "Saffron"),
            category = BeverageCategory.MILK,
            drawableRes = "ic_beverage_badam_milk",
            hasSugarOption = true
        ),
        Beverage(
            id = "4",
            name = "Milk",
            description = "Fresh warm milk",
            price = 15.0,
            imageUrl = "",
            ingredients = listOf("Milk"),
            category = BeverageCategory.MILK,
            drawableRes = "ic_beverage_milk",
            hasSugarOption = true
        ),
        Beverage(
            id = "5",
            name = "Dry Ginger Tea",
            description = "Spicy dry ginger tea",
            price = 20.0,
            imageUrl = "",
            ingredients = listOf("Dry ginger", "Tea leaves", "Water", "Sugar"),
            category = BeverageCategory.TEA,
            drawableRes = "ic_beverage_dry_ginger_tea",
            hasSugarOption = true
        ),
        Beverage(
            id = "6",
            name = "Black Coffee",
            description = "Strong black coffee without milk",
            price = 20.0,
            imageUrl = "",
            ingredients = listOf("Coffee powder", "Water"),
            category = BeverageCategory.COFFEE,
            drawableRes = "ic_beverage_black_coffee",
            hasSugarOption = true
        ),
        Beverage(
            id = "7",
            name = "Horlicks",
            description = "Warm Horlicks health drink",
            price = 25.0,
            imageUrl = "",
            ingredients = listOf("Horlicks powder", "Milk", "Sugar"),
            category = BeverageCategory.HEALTH_DRINK,
            drawableRes = "ic_beverage_horlicks",
            hasSugarOption = true
        ),
        Beverage(
            id = "8",
            name = "Boost",
            description = "Energy Boost health drink",
            price = 25.0,
            imageUrl = "",
            ingredients = listOf("Boost powder", "Milk", "Sugar"),
            category = BeverageCategory.HEALTH_DRINK,
            drawableRes = "ic_beverage_boost",
            hasSugarOption = true
        )
    )
}
