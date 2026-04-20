package com.nktranslator.network

import com.google.gson.annotations.SerializedName

// ── Request ──────────────────────────────────────────────────────────
data class AnthropicRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val system: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

// ── Response ─────────────────────────────────────────────────────────
data class AnthropicResponse(
    val id: String?,
    val type: String?,
    val role: String?,
    val content: List<ContentBlock>?,
    val error: AnthropicError?
)

data class ContentBlock(
    val type: String,
    val text: String?
)

data class AnthropicError(
    val type: String,
    val message: String
)
