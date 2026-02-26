package com.marginalia.scaffold

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ProjectScaffolderTest {

    private val fakeGitInit: (Path) -> GitInitResult = { dir ->
        GitInitResult(dir, success = true, output = "Initialized")
    }

    private fun config(rootDir: Path) = ProjectConfig(
        name = "myproject",
        rootDir = rootDir,
        language = "Kotlin",
        description = "A test project",
    )

    @Test
    fun `scaffold creates specs and code directories`(@TempDir tempDir: Path) {
        val scaffolder = ProjectScaffolder(fakeGitInit)
        val result = scaffolder.scaffold(config(tempDir))

        assertTrue(Files.isDirectory(result.specsDir))
        assertTrue(Files.isDirectory(result.codeDir))
        assertEquals(tempDir.resolve("myproject-specs"), result.specsDir)
        assertEquals(tempDir.resolve("myproject"), result.codeDir)
    }

    @Test
    fun `scaffold creates spec file structure`(@TempDir tempDir: Path) {
        val scaffolder = ProjectScaffolder(fakeGitInit)
        val result = scaffolder.scaffold(config(tempDir))

        assertTrue(Files.isRegularFile(result.specsDir.resolve("README.md")))
        assertTrue(Files.isRegularFile(result.specsDir.resolve("product-description.md")))
        assertTrue(Files.isDirectory(result.specsDir.resolve("epics")))
        assertTrue(Files.isDirectory(result.specsDir.resolve("stories")))
        assertTrue(Files.isRegularFile(result.specsDir.resolve("epics/_index.md")))
        assertTrue(Files.isRegularFile(result.specsDir.resolve("stories/_index.md")))
    }

    @Test
    fun `scaffold populates spec files with correct content`(@TempDir tempDir: Path) {
        val scaffolder = ProjectScaffolder(fakeGitInit)
        val result = scaffolder.scaffold(config(tempDir))

        val readme = Files.readString(result.specsDir.resolve("README.md"))
        assertTrue(readme.contains("myproject"))

        val productDesc = Files.readString(result.specsDir.resolve("product-description.md"))
        assertTrue(productDesc.contains("myproject"))
        assertTrue(productDesc.contains("A test project"))

        val epicsIndex = Files.readString(result.specsDir.resolve("epics/_index.md"))
        assertTrue(epicsIndex.contains("Epics Index"))

        val storiesIndex = Files.readString(result.specsDir.resolve("stories/_index.md"))
        assertTrue(storiesIndex.contains("Stories Index"))
    }

    @Test
    fun `scaffold calls gitInitRunner for both directories`(@TempDir tempDir: Path) {
        val initedDirs = mutableListOf<Path>()
        val trackingGitInit: (Path) -> GitInitResult = { dir ->
            initedDirs.add(dir)
            GitInitResult(dir, success = true, output = "Initialized")
        }

        val scaffolder = ProjectScaffolder(trackingGitInit)
        val result = scaffolder.scaffold(config(tempDir))

        assertEquals(2, initedDirs.size)
        assertTrue(initedDirs.contains(result.specsDir))
        assertTrue(initedDirs.contains(result.codeDir))
    }

    @Test
    fun `scaffold returns git init results`(@TempDir tempDir: Path) {
        val scaffolder = ProjectScaffolder(fakeGitInit)
        val result = scaffolder.scaffold(config(tempDir))

        assertEquals(2, result.gitInitResults.size)
        assertTrue(result.gitInitResults.all { it.success })
    }

    @Test
    fun `scaffold fails when root directory does not exist`(@TempDir tempDir: Path) {
        val scaffolder = ProjectScaffolder(fakeGitInit)
        val badConfig = config(tempDir.resolve("nonexistent"))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            scaffolder.scaffold(badConfig)
        }
        assertTrue(exception.message!!.contains("Root directory"))
    }

    @Test
    fun `scaffold fails when specs directory already exists`(@TempDir tempDir: Path) {
        Files.createDirectory(tempDir.resolve("myproject-specs"))
        val scaffolder = ProjectScaffolder(fakeGitInit)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            scaffolder.scaffold(config(tempDir))
        }
        assertTrue(exception.message!!.contains("Specs directory already exists"))
    }

    @Test
    fun `scaffold succeeds when code directory already exists`(@TempDir tempDir: Path) {
        Files.createDirectory(tempDir.resolve("myproject"))
        val scaffolder = ProjectScaffolder(fakeGitInit)

        val result = scaffolder.scaffold(config(tempDir))

        assertTrue(Files.isDirectory(result.codeDir))
        assertTrue(Files.isDirectory(result.specsDir))
    }
}
