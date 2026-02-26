package com.marginalia.ai

import java.nio.file.Files
import java.nio.file.Path

/**
 * Discovers the Claude Code CLI binary from well-known locations.
 *
 * Checks in priority order:
 * 1. User-configured path (passed as parameter)
 * 2. ~/.claude/local/claude
 * 3. /usr/local/bin/claude (Unix) or %LOCALAPPDATA%\Programs\claude\claude.exe (Windows)
 * 4. System PATH via `which`/`where`
 */
object ClaudeCliDiscovery {

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    @Volatile
    private var cached: Path? = null

    @Volatile
    private var cacheValid = false

    /**
     * Returns the path to the `claude` binary, or null if not found.
     *
     * @param userConfiguredPath optional path from plugin settings; checked first if non-blank
     */
    fun discover(userConfiguredPath: String? = null): Path? {
        if (cacheValid) return cached

        val result = doDiscover(userConfiguredPath)
        cached = result
        cacheValid = true
        return result
    }

    /** Invalidates the cached result so the next [discover] call re-scans. */
    fun invalidateCache() {
        cacheValid = false
        cached = null
    }

    /** Returns true if the given path exists and is executable. */
    fun isUsable(path: Path): Boolean =
        Files.exists(path) && Files.isExecutable(path)

    private fun doDiscover(userConfiguredPath: String?): Path? {
        // 1. User-configured path
        if (!userConfiguredPath.isNullOrBlank()) {
            val path = Path.of(userConfiguredPath)
            if (isUsable(path)) return path
        }

        // 2. ~/.claude/local/claude
        val home = System.getProperty("user.home")
        val localClaude = Path.of(home, ".claude", "local", "claude")
        if (isUsable(localClaude)) return localClaude

        // 3. Platform-specific well-known location
        val platformPath = if (isWindows) {
            val localAppData = System.getenv("LOCALAPPDATA")
            if (localAppData != null) Path.of(localAppData, "Programs", "claude", "claude.exe") else null
        } else {
            Path.of("/usr/local/bin/claude")
        }
        if (platformPath != null && isUsable(platformPath)) return platformPath

        // 4. System PATH lookup
        return findOnPath()
    }

    private fun findOnPath(): Path? {
        val command = if (isWindows) listOf("where", "claude") else listOf("which", "claude")
        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return null
            }
            if (process.exitValue() != 0) return null
            val output = process.inputStream.bufferedReader().readText().trim()
            if (output.isNotEmpty()) {
                val path = Path.of(output.lines().first())
                if (isUsable(path)) path else null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
