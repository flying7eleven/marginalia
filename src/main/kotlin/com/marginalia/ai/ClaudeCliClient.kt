package com.marginalia.ai

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class ClaudeCliClient(
    private val cliBinary: Path,
    private val timeoutSeconds: Long = 120,
) : AiClient {

    private var sessionId: String? = null

    override suspend fun chat(systemPrompt: String, messages: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            val userMessage = messages.lastOrNull { it.role == Role.USER }?.content
                ?: error("No user message found in messages list")

            val command = buildCommand(systemPrompt, userMessage)
            val result = execute(command)

            val json = JsonParser.parseString(result).asJsonObject
            if (sessionId == null) {
                sessionId = json.get("session_id")?.asString
            }

            json.get("result")?.asString
                ?: error("Missing 'result' field in CLI response")
        }

    private fun buildCommand(systemPrompt: String, message: String): List<String> {
        val cmd = mutableListOf(cliBinary.toString(), "-p")

        val currentSessionId = sessionId
        if (currentSessionId != null) {
            cmd += listOf("--resume", currentSessionId)
        } else if (systemPrompt.isNotBlank()) {
            cmd += listOf("--system-prompt", systemPrompt)
        }

        cmd += listOf("--output-format", "json")
        cmd += message

        return cmd
    }

    private fun execute(command: List<String>): String {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            error("Claude CLI timed out after ${timeoutSeconds}s")
        }

        val output = process.inputStream.bufferedReader().readText()

        if (process.exitValue() != 0) {
            error("Claude CLI exited with code ${process.exitValue()}: $output")
        }

        return output
    }
}
