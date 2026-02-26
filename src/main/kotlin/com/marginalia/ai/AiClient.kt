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
 *
 * @param systemPrompt the system prompt; non-null only on the first call of a conversation
 * @param message the latest user message to send
 */
interface AiClient {
    suspend fun chat(systemPrompt: String?, message: String): String
}
