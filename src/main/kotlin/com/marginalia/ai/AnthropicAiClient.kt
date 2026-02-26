package com.marginalia.ai

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [AiClient] implementation backed by the Anthropic Messages API.
 */
class AnthropicAiClient(
    apiKey: String,
    private val model: String = "claude-sonnet-4-20250514",
) : AiClient {

    private val client: AnthropicClient = AnthropicOkHttpClient.builder()
        .apiKey(apiKey)
        .build()

    override suspend fun chat(systemPrompt: String, messages: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            val builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(4096)
                .system(systemPrompt)

            for (msg in messages) {
                when (msg.role) {
                    Role.USER -> builder.addUserMessage(msg.content)
                    Role.ASSISTANT -> builder.addAssistantMessage(msg.content)
                }
            }

            val response = client.messages().create(builder.build())

            response.content()
                .mapNotNull { block -> block.text().orElse(null)?.text() }
                .joinToString("")
        }
}
