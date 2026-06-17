package com.jarvis.assistant.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Thin wrapper around the Anthropic Messages API.
 * Requires a real API key from https://console.anthropic.com — API usage is billed
 * per token, separate from any Claude.ai subscription.
 */
class ClaudeApiClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    fun sendMessage(
        systemPrompt: String,
        userMessage: String,
        model: String = "claude-sonnet-4-6"
    ): Result<String> {
        return try {
            val body = JSONObject().apply {
                put("model", model)
                put("max_tokens", 1500)
                put("system", systemPrompt)
                put(
                    "messages",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", userMessage)
                        }
                    )
                )
            }

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IOException("API error ${response.code}: $responseBody"))
                }
                val json = JSONObject(responseBody)
                val content = json.getJSONArray("content")
                val text = StringBuilder()
                for (i in 0 until content.length()) {
                    val block = content.getJSONObject(i)
                    if (block.optString("type") == "text") {
                        text.append(block.optString("text"))
                    }
                }
                Result.success(text.toString())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
