package com.marginalia.scaffold

import java.nio.file.Path

data class ProjectConfig(
    val name: String,
    val rootDir: Path,
    val language: String,
    val description: String,
)
