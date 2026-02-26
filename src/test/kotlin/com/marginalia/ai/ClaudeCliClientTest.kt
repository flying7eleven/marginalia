package com.marginalia.ai

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ClaudeCliClientTest {

    @Test
    fun `first call uses system-prompt flag`(@TempDir tempDir: Path) = runTest {
        val script = createFakeClaudeScript(tempDir, """{"session_id":"abc-123","result":"Hello!"}""")
        val client = ClaudeCliClient(script, timeoutSeconds = 10)

        val response = client.chat(
            "You are a helpful assistant",
            listOf(ChatMessage(Role.USER, "Hi")),
        )

        assertEquals("Hello!", response)
    }

    @Test
    fun `subsequent calls use resume flag`(@TempDir tempDir: Path) = runTest {
        val script = createFakeClaudeScript(tempDir, """{"session_id":"abc-123","result":"Response"}""")
        val client = ClaudeCliClient(script, timeoutSeconds = 10)

        // First call establishes session
        client.chat("System prompt", listOf(ChatMessage(Role.USER, "First")))
        // Second call should use --resume
        val response = client.chat("System prompt", listOf(ChatMessage(Role.USER, "Second")))

        assertEquals("Response", response)
    }

    @Test
    fun `throws on non-zero exit code`(@TempDir tempDir: Path) = runTest {
        val script = createFakeClaudeScript(tempDir, "Error: not authenticated", exitCode = 1)
        val client = ClaudeCliClient(script, timeoutSeconds = 10)

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.test.runTest {
                client.chat("prompt", listOf(ChatMessage(Role.USER, "Hi")))
            }
        }
        assertTrue(exception.message!!.contains("exited with code 1"))
    }

    @Test
    fun `throws on timeout`(@TempDir tempDir: Path) = runTest {
        val script = createFakeClaudeScript(tempDir, "", sleepSeconds = 5)
        val client = ClaudeCliClient(script, timeoutSeconds = 1)

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.test.runTest {
                client.chat("prompt", listOf(ChatMessage(Role.USER, "Hi")))
            }
        }
        assertTrue(exception.message!!.contains("timed out"))
    }

    @Test
    fun `throws when no user message provided`(@TempDir tempDir: Path) = runTest {
        val script = createFakeClaudeScript(tempDir, """{"session_id":"x","result":"ok"}""")
        val client = ClaudeCliClient(script, timeoutSeconds = 10)

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.test.runTest {
                client.chat("prompt", emptyList())
            }
        }
        assertTrue(exception.message!!.contains("No user message"))
    }

    @Test
    fun `extracts session_id from first response`(@TempDir tempDir: Path) = runTest {
        val responses = mutableListOf(
            """{"session_id":"session-42","result":"First response"}""",
            """{"session_id":"session-42","result":"Second response"}""",
        )
        val script = createMultiResponseScript(tempDir, responses)
        val client = ClaudeCliClient(script, timeoutSeconds = 10)

        client.chat("prompt", listOf(ChatMessage(Role.USER, "Hi")))
        val second = client.chat("prompt", listOf(ChatMessage(Role.USER, "Follow up")))

        assertEquals("Second response", second)
    }

    private fun createFakeClaudeScript(
        dir: Path,
        output: String,
        exitCode: Int = 0,
        sleepSeconds: Int = 0,
    ): Path {
        val script = dir.resolve("claude")
        val content = buildString {
            appendLine("#!/bin/sh")
            if (sleepSeconds > 0) {
                appendLine("sleep $sleepSeconds")
            }
            if (output.isNotEmpty()) {
                appendLine("echo '${output.replace("'", "'\\''")}'")
            }
            appendLine("exit $exitCode")
        }
        Files.writeString(script, content)
        script.toFile().setExecutable(true)
        return script
    }

    private fun createMultiResponseScript(dir: Path, responses: List<String>): Path {
        val script = dir.resolve("claude")
        val stateFile = dir.resolve(".call_count")
        val content = buildString {
            appendLine("#!/bin/sh")
            appendLine("STATE_FILE='${stateFile}'")
            appendLine("if [ -f \"\$STATE_FILE\" ]; then")
            appendLine("  COUNT=\$(cat \"\$STATE_FILE\")")
            appendLine("else")
            appendLine("  COUNT=0")
            appendLine("fi")
            for ((i, response) in responses.withIndex()) {
                val condition = if (i == 0) "if" else "elif"
                appendLine("$condition [ \"\$COUNT\" -eq $i ]; then")
                appendLine("  echo '${response.replace("'", "'\\''")}'")
            }
            appendLine("fi")
            appendLine("COUNT=\$((COUNT + 1))")
            appendLine("echo \"\$COUNT\" > \"\$STATE_FILE\"")
        }
        Files.writeString(script, content)
        script.toFile().setExecutable(true)
        return script
    }
}
