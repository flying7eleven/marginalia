package com.marginalia.scaffold

import java.nio.file.Path

data class GitInitResult(
    val directory: Path,
    val success: Boolean,
    val output: String,
)
