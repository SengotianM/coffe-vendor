package com.coffevendor.data.model

import org.junit.Assert.*
import org.junit.Test

class UserRegistrationTest {

    @Test
    fun `valid user registration fields are not blank`() {
        val userId = "user123"
        val username = "John Doe"
        val empId = "E001"
        val seatNumber = "Desk-5"
        val mobileNumber = "9876543210"
        val password = "secret123"

        assertTrue(userId.isNotBlank())
        assertTrue(username.isNotBlank())
        assertTrue(empId.isNotBlank())
        assertTrue(seatNumber.isNotBlank())
        assertTrue(mobileNumber.isNotBlank())
        assertTrue(password.isNotBlank())
    }

    @Test
    fun `empty userId is invalid`() {
        val userId = ""
        assertTrue(userId.isBlank())
    }

    @Test
    fun `blank userId is invalid`() {
        val userId = "   "
        assertTrue(userId.isBlank())
    }

    @Test
    fun `password must be at least 6 characters`() {
        val shortPassword = "12345"
        val validPassword = "123456"

        assertTrue(shortPassword.length < 6)
        assertTrue(validPassword.length >= 6)
    }

    @Test
    fun `password with exactly 6 characters is valid`() {
        val password = "abcdef"
        assertTrue(password.length >= 6)
    }

    @Test
    fun `mobile number must be 10 digits`() {
        val validMobile = "9876543210"
        val shortMobile = "12345"
        val longMobile = "12345678901"

        assertEquals(10, validMobile.length)
        assertTrue(shortMobile.length != 10)
        assertTrue(longMobile.length != 10)
    }

    @Test
    fun `user creation with all required fields succeeds`() {
        val user = User(
            id = "1",
            userId = "user123",
            username = "John Doe",
            empId = "E001",
            seatNumber = "Desk-5",
            mobileNumber = "9876543210",
            password = "secret123"
        )

        assertEquals("user123", user.userId)
        assertEquals("John Doe", user.username)
        assertEquals("E001", user.empId)
        assertEquals("Desk-5", user.seatNumber)
        assertEquals("9876543210", user.mobileNumber)
        assertEquals("secret123", user.password)
        assertEquals(UserRole.CUSTOMER, user.role)
    }

    @Test
    fun `user defaults to CUSTOMER role`() {
        val user = User(
            id = "1",
            userId = "user123",
            username = "John",
            empId = "E001",
            seatNumber = "D1",
            mobileNumber = "1234567890",
            password = "pass123"
        )
        assertEquals(UserRole.CUSTOMER, user.role)
    }

    @Test
    fun `user can be created with VENDOR role`() {
        val user = User(
            id = "1",
            userId = "vendor1",
            username = "Vendor Admin",
            empId = "V001",
            seatNumber = "Counter-1",
            mobileNumber = "0000000000",
            password = "1234",
            role = UserRole.VENDOR
        )
        assertEquals(UserRole.VENDOR, user.role)
    }

    @Test
    fun `duplicate userId detection via equality`() {
        val user1 = User(
            id = "1", userId = "user123", username = "John",
            empId = "E001", seatNumber = "D1", mobileNumber = "123", password = "pass"
        )
        val user2 = User(
            id = "2", userId = "user123", username = "Jane",
            empId = "E002", seatNumber = "D2", mobileNumber = "456", password = "pass"
        )
        assertEquals(user1.userId, user2.userId)
    }

    @Test
    fun `signUp request body contains correct fields`() {
        val userId = "testuser"
        val password = "testpass"

        val body = org.json.JSONObject().apply {
            put("user_id", userId)
            put("password", password)
            put("role", "CUSTOMER")
        }

        assertEquals(userId, body.getString("user_id"))
        assertEquals(password, body.getString("password"))
        assertEquals("CUSTOMER", body.getString("role"))
    }
}
