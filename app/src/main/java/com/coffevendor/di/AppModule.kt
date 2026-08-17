package com.coffevendor.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.coffevendor.data.local.BeverageDao
import com.coffevendor.data.local.CoffeeDatabase
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.UserDao
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    username TEXT NOT NULL,
                    empId TEXT NOT NULL,
                    seatNumber TEXT NOT NULL,
                    mobileNumber TEXT NOT NULL,
                    password TEXT NOT NULL,
                    photoUri TEXT,
                    favoriteBeverages TEXT NOT NULL DEFAULT '',
                    isBiometricEnabled INTEGER NOT NULL DEFAULT 0,
                    isLoggedIn INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE orders ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoffeeDatabase {
        return Room.databaseBuilder(
            context,
            CoffeeDatabase::class.java,
            "coffee_vendor_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
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
    fun provideUserDao(database: CoffeeDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideApiService(): CoffeeApiService {
        return ApiClient.apiService
    }
}
