package com.coffevendor.data.remote

import com.coffevendor.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CoffeeApiService {

    @GET("beverages")
    suspend fun getBeverages(): Response<BeverageListResponse>

    @GET("beverages/{id}")
    suspend fun getBeverage(@Path("id") id: String): Response<Beverage>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<OrderResponse>

    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("vendor_id") vendorId: String? = null
    ): Response<List<Order>>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    @DELETE("orders/{id}")
    suspend fun cancelOrder(@Path("id") id: String): Response<OrderResponse>

    @GET("boardrooms")
    suspend fun getBoardrooms(): Response<BoardroomListResponse>

    @GET("vendor/orders/active")
    suspend fun getActiveVendorOrders(): Response<List<Order>>

    @PATCH("orders/{id}/serve")
    suspend fun markAsServed(@Path("id") id: String): Response<OrderResponse>
}
