package com.marginalia.ai

/**
 * Role in a chat conversation.
 */
enum class Role { USER, ASSISTANT }

/**
 * A single message in a chat conversation.
 */
data class ChatMessage(val role: Role, val content: String)

/**
 * Minimal AI client interface for the interview engine.
 */
interface AiClient {
    suspend fun chat(systemPrompt: String, messages: List<ChatMessage>): String
}
