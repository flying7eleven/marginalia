package com.marginalia.scaffold

import java.nio.file.Path
import java.util.concurrent.TimeUnit

fun gitInit(directory: Path): GitInitResult {
    return try {
        val process = ProcessBuilder("git", "init")
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exited = process.waitFor(30, TimeUnit.SECONDS)

        GitInitResult(
            directory = directory,
            success = exited && process.exitValue() == 0,
            output = output.trim(),
        )
    } catch (e: Exception) {
        GitInitResult(
            directory = directory,
            success = false,
            output = e.message ?: "Unknown error",
        )
    }
}
