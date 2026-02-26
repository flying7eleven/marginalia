package com.marginalia.scaffold

import java.nio.file.Files
import java.nio.file.Path

class ProjectScaffolder(
    private val gitInitRunner: (Path) -> GitInitResult = ::gitInit,
) {

    fun scaffold(config: ProjectConfig): ScaffoldResult {
        val specsDir = config.rootDir.resolve("${config.name}-specs")
        val codeDir = config.rootDir.resolve(config.name)

        require(Files.isDirectory(config.rootDir)) {
            "Root directory does not exist or is not a directory: ${config.rootDir}"
        }
        require(!Files.exists(specsDir)) {
            "Specs directory already exists: $specsDir"
        }
        require(!Files.exists(codeDir)) {
            "Code directory already exists: $codeDir"
        }

        Files.createDirectory(specsDir)
        Files.createDirectory(codeDir)

        val epicsDir = specsDir.resolve("epics")
        val storiesDir = specsDir.resolve("stories")
        Files.createDirectory(epicsDir)
        Files.createDirectory(storiesDir)

        Files.writeString(specsDir.resolve("README.md"), SpecTemplates.readme(config.name, config.description))
        Files.writeString(specsDir.resolve("product-description.md"), SpecTemplates.productDescription(config.name, config.description))
        Files.writeString(epicsDir.resolve("_index.md"), SpecTemplates.epicsIndex())
        Files.writeString(storiesDir.resolve("_index.md"), SpecTemplates.storiesIndex())

        val gitInitResults = listOf(
            gitInitRunner(specsDir),
            gitInitRunner(codeDir),
        )

        return ScaffoldResult(
            specsDir = specsDir,
            codeDir = codeDir,
            gitInitResults = gitInitResults,
        )
    }
}
