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

    private val history = mutableListOf<ChatMessage>()

    override suspend fun chat(systemPrompt: String?, message: String): String =
        withContext(Dispatchers.IO) {
            history.add(ChatMessage(Role.USER, message))

            val builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(4096)

            if (!systemPrompt.isNullOrBlank()) {
                builder.system(systemPrompt)
            }

            for (msg in history) {
                when (msg.role) {
                    Role.USER -> builder.addUserMessage(msg.content)
                    Role.ASSISTANT -> builder.addAssistantMessage(msg.content)
                }
            }

            val response = client.messages().create(builder.build())

            val text = response.content()
                .mapNotNull { block -> block.text().orElse(null)?.text() }
                .joinToString("")

            history.add(ChatMessage(Role.ASSISTANT, text))
            text
        }
}
