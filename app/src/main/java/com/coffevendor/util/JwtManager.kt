package com.coffevendor.util

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.UUID

object JwtManager {

    private const val SECRET = "coffee-vendor-jwt-secret-2026"
    private const val ACCESS_TOKEN_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000   // 30 days
    private const val REFRESH_TOKEN_EXPIRY_MS = 7L * 24 * 60 * 60 * 1000   // 7 days

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
        val accessTokenExpiry: Long,
        val refreshTokenExpiry: Long
    )

    fun generateTokenPair(userId: String): TokenPair {
        val now = System.currentTimeMillis()
        val accessExpiry = now + ACCESS_TOKEN_EXPIRY_MS
        val refreshExpiry = now + REFRESH_TOKEN_EXPIRY_MS

        val accessToken = createToken(userId, "access", accessExpiry)
        val refreshToken = UUID.randomUUID().toString()

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = accessExpiry,
            refreshTokenExpiry = refreshExpiry
        )
    }

    fun validateAccessToken(token: String): Boolean {
        return try {
            val payload = decodePayload(token) ?: return false
            val expiry = payload.getLong("exp")
            System.currentTimeMillis() < expiry
        } catch (_: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): String? {
        return try {
            decodePayload(token)?.getString("sub")
        } catch (_: Exception) {
            null
        }
    }

    fun isRefreshTokenValid(refreshToken: String, expiry: Long): Boolean {
        return refreshToken.isNotBlank() && System.currentTimeMillis() < expiry
    }

    fun refreshAccessToken(refreshToken: String, userId: String, refreshExpiry: Long): TokenPair? {
        if (!isRefreshTokenValid(refreshToken, refreshExpiry)) return null
        val newExpiry = System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS
        val newAccessToken = createToken(userId, "access", newExpiry)
        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = newExpiry,
            refreshTokenExpiry = refreshExpiry
        )
    }

    private fun createToken(userId: String, type: String, expiry: Long): String {
        val header = encodeJson(mapOf("alg" to "HS256", "typ" to "JWT"))
        val payload = encodeJson(mapOf(
            "sub" to userId,
            "type" to type,
            "exp" to expiry,
            "iat" to System.currentTimeMillis()
        ))
        val signature = hmacSha256("$header.$payload")
        return "$header.$payload.$signature"
    }

    private fun hmacSha256(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hash, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun encodeJson(map: Map<String, Any>): String {
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        return Base64.encodeToString(
            json.toString().toByteArray(StandardCharsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun decodePayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            JSONObject(String(payloadBytes, StandardCharsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }
}
