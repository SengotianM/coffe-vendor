package com.coffevendor.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.coffevendor.data.local.BeverageDao
import com.coffevendor.data.local.CoffeeDatabase
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.remote.SupabaseRepository
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

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'CUSTOMER'")
            database.execSQL("""
                INSERT OR IGNORE INTO users (id, userId, username, empId, seatNumber, mobileNumber, password, photoUri, favoriteBeverages, isBiometricEnabled, isLoggedIn, role, accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry)
                VALUES ('vendor_001', 'vendor', 'Coffee Vendor', 'V001', 'Counter-1', '0000000000', '1234', NULL, '', 0, 0, 'VENDOR', '', '', 0, 0)
            """.trimIndent())
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE users ADD COLUMN accessToken TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN refreshToken TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN accessTokenExpiry INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN refreshTokenExpiry INTEGER NOT NULL DEFAULT 0")
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("""
                    INSERT OR IGNORE INTO users (id, userId, username, empId, seatNumber, mobileNumber, password, photoUri, favoriteBeverages, isBiometricEnabled, isLoggedIn, role, accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry)
                    VALUES ('vendor_001', 'vendor', 'Coffee Vendor', 'V001', 'Counter-1', '0000000000', '1234', NULL, '', 0, 0, 'VENDOR', '', '', 0, 0)
                """.trimIndent())
            }
        })
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
    fun provideSupabaseRepository(
        beverageDao: BeverageDao,
        orderDao: OrderDao,
        userDao: UserDao
    ): SupabaseRepository {
        return SupabaseRepository(beverageDao, orderDao, userDao)
    }
}
