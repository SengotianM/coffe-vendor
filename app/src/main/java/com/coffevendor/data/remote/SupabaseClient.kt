package com.coffevendor.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val BASE_URL = "https://trxoycjvstwslwueltpb.supabase.co"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRyeG95Y2p2c3R3c2x3dWVsdHBiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY5NzEyMjIsImV4cCI6MjEwMjU0NzIyMn0.7tlQoI2JOl_Hw4x62Itht59jPJmKcdrheyW48kv00E8"
    private const val REST_URL = "$BASE_URL/rest/v1"

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun headers(): Map<String, String> = mapOf(
        "apikey" to ANON_KEY,
        "Authorization" to "Bearer $ANON_KEY",
        "Content-Type" to "application/json",
        "Prefer" to "return=representation"
    )

    fun get(table: String, filter: String? = null): String {
        val url = if (filter != null) "$REST_URL/$table?$filter" else "$REST_URL/$table"
        android.util.Log.d("SupabaseClient", "GET $url")
        val request = Request.Builder()
            .url(url)
            .headers(headers().toHeaders())
            .get()
            .build()
        val response = client.newCall(request).execute()
        val code = response.code
        val body = response.body?.string() ?: ""
        android.util.Log.d("SupabaseClient", "GET $table -> code=$code body=${body.take(500)}")
        if (code !in 200..299) {
            throw RuntimeException("Supabase get failed ($code): $body")
        }
        return body
    }

    fun insert(table: String, jsonBody: String): String {
        val request = Request.Builder()
            .url("$REST_URL/$table")
            .headers(headers().toHeaders())
            .post(jsonBody.toRequestBody(JSON_MEDIA))
            .build()
        val response = client.newCall(request).execute()
        val code = response.code
        val body = response.body?.string() ?: ""
        android.util.Log.d("SupabaseClient", "INSERT $table -> code=$code body=$body")
        if (code !in 200..299) {
            throw RuntimeException("Supabase insert failed ($code): $body")
        }
        return body
    }

    fun insertMultiple(table: String, jsonArray: String): String {
        val request = Request.Builder()
            .url("$REST_URL/$table")
            .headers(headers().toHeaders())
            .post(jsonArray.toRequestBody(JSON_MEDIA))
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    fun update(table: String, jsonBody: String, filter: String): String {
        val request = Request.Builder()
            .url("$REST_URL/$table?$filter")
            .headers(headers().toHeaders())
            .patch(jsonBody.toRequestBody(JSON_MEDIA))
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    fun delete(table: String, filter: String): String {
        val request = Request.Builder()
            .url("$REST_URL/$table?$filter")
            .headers(headers().toHeaders())
            .delete()
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    private fun Map<String, String>.toHeaders(): okhttp3.Headers {
        val builder = okhttp3.Headers.Builder()
        forEach { (k, v) -> builder.add(k, v) }
        return builder.build()
    }
}
