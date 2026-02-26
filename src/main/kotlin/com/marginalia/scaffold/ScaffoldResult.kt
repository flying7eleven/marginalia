package com.marginalia.scaffold

import java.nio.file.Path

data class ScaffoldResult(
    val specsDir: Path,
    val codeDir: Path,
    val gitInitResults: List<GitInitResult>,
)
