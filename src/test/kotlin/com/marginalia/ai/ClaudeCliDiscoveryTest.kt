package com.marginalia.ai

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ClaudeCliDiscoveryTest {

    @AfterEach
    fun tearDown() {
        ClaudeCliDiscovery.invalidateCache()
    }

    @Test
    fun `isUsable returns true for existing executable`(@TempDir dir: Path) {
        val binary = createExecutable(dir, "claude")
        assertTrue(ClaudeCliDiscovery.isUsable(binary))
    }

    @Test
    fun `isUsable returns false for non-existent path`() {
        assertFalse(ClaudeCliDiscovery.isUsable(Path.of("/nonexistent/claude")))
    }

    @Test
    fun `isUsable returns false for non-executable file`(@TempDir dir: Path) {
        val file = dir.resolve("claude")
        Files.writeString(file, "not executable")
        file.toFile().setExecutable(false)
        assertFalse(ClaudeCliDiscovery.isUsable(file))
    }

    @Test
    fun `discover returns user-configured path when usable`(@TempDir dir: Path) {
        val binary = createExecutable(dir, "claude")
        val result = ClaudeCliDiscovery.discover(userConfiguredPath = binary.toString())
        assertEquals(binary, result)
    }

    @Test
    fun `discover skips blank user-configured path`(@TempDir dir: Path) {
        // With blank user path and no well-known locations, should fall through
        val result = ClaudeCliDiscovery.discover(userConfiguredPath = "  ")
        // Result depends on whether claude is actually installed on this machine,
        // but at minimum it should not throw
        assertDoesNotThrow { result }
    }

    @Test
    fun `discover skips non-existent user-configured path`() {
        val result = ClaudeCliDiscovery.discover(userConfiguredPath = "/nonexistent/path/claude")
        // Should not return the invalid path
        if (result != null) {
            assertNotEquals(Path.of("/nonexistent/path/claude"), result)
        }
    }

    @Test
    fun `discover caches result`(@TempDir dir: Path) {
        val binary = createExecutable(dir, "claude")
        val first = ClaudeCliDiscovery.discover(userConfiguredPath = binary.toString())
        // Delete the binary — cached result should still be returned
        Files.delete(binary)
        val second = ClaudeCliDiscovery.discover(userConfiguredPath = binary.toString())
        assertEquals(first, second)
    }

    @Test
    fun `invalidateCache forces re-discovery`(@TempDir dir: Path) {
        val binary = createExecutable(dir, "claude")
        val first = ClaudeCliDiscovery.discover(userConfiguredPath = binary.toString())
        assertEquals(binary, first)

        ClaudeCliDiscovery.invalidateCache()
        // After invalidation with a non-existent user path, should re-discover
        val second = ClaudeCliDiscovery.discover(userConfiguredPath = "/nonexistent/claude")
        assertNotEquals(binary, second)
    }

    private fun createExecutable(dir: Path, name: String): Path {
        val file = dir.resolve(name)
        Files.writeString(file, "#!/bin/sh\necho ok")
        file.toFile().setExecutable(true)
        return file
    }
}
