package com.coffevendor.di

import android.content.Context
import androidx.room.Room
import com.coffevendor.data.local.BeverageDao
import com.coffevendor.data.local.CoffeeDatabase
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.remote.ApiClient
import com.coffevendor.data.remote.CoffeeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoffeeDatabase {
        return Room.databaseBuilder(
            context,
            CoffeeDatabase::class.java,
            "coffee_vendor_db"
        ).build()
    }

    @Provides
    fun provideBeverageDao(database: CoffeeDatabase): BeverageDao {
        return database.beverageDao()
    }

    @Provides
    fun provideOrderDao(database: CoffeeDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideApiService(): CoffeeApiService {
        return ApiClient.apiService
    }
}
