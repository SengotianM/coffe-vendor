package com.coffevendor.data.local

import androidx.room.*
import com.coffevendor.data.model.*

@Entity(tableName = "beverages")
data class BeverageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val ingredients: String,
    val category: String,
    val drawableRes: String,
    val hasSugarOption: Boolean,
    val isAvailable: Boolean
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val beverageId: String,
    val beverageName: String,
    val quantity: Int,
    val locationType: String,
    val seatOrRow: String?,
    val hallName: String?,
    val targetTime: String,
    val recurrence: String,
    val createdAt: String,
    val status: String,
    val specialInstructions: String?
)

fun Beverage.toEntity(): BeverageEntity = BeverageEntity(
    id = id,
    name = name,
    description = description,
    price = price,
    imageUrl = imageUrl,
    ingredients = ingredients.joinToString(","),
    category = category.name,
    drawableRes = drawableRes,
    hasSugarOption = hasSugarOption,
    isAvailable = isAvailable
)

fun BeverageEntity.toDomain(): Beverage = Beverage(
    id = id,
    name = name,
    description = description,
    price = price,
    imageUrl = imageUrl,
    ingredients = ingredients.split(",").filter { it.isNotBlank() },
    category = BeverageCategory.valueOf(category),
    drawableRes = drawableRes,
    hasSugarOption = hasSugarOption,
    isAvailable = isAvailable
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    beverageId = beverageId,
    beverageName = beverageName,
    quantity = quantity,
    locationType = location.type.name,
    seatOrRow = location.seatOrRow,
    hallName = location.hallName,
    targetTime = targetTime,
    recurrence = recurrence.name,
    createdAt = createdAt,
    status = status.name,
    specialInstructions = specialInstructions
)

fun OrderEntity.toDomain(): Order = Order(
    id = id,
    beverageId = beverageId,
    beverageName = beverageName,
    quantity = quantity,
    location = DeliveryLocation(
        type = LocationType.valueOf(locationType),
        seatOrRow = seatOrRow,
        hallName = hallName
    ),
    targetTime = targetTime,
    recurrence = RecurrenceType.valueOf(recurrence),
    createdAt = createdAt,
    status = OrderStatus.valueOf(status),
    specialInstructions = specialInstructions
)
