package com.marginalia.scaffold

import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path

@Service(Service.Level.APP)
class ScaffoldService {

    internal var scaffolder: (ProjectConfig) -> ScaffoldResult =
        ProjectScaffolder()::scaffold

    internal var vfsRefresh: (Path) -> Unit = { path ->
        LocalFileSystem.getInstance().refreshNioFiles(listOf(path), true, true, null)
    }

    fun scaffold(config: ProjectConfig): ScaffoldResult {
        val result = scaffolder(config)
        vfsRefresh(result.specsDir)
        vfsRefresh(result.codeDir)
        return result
    }
}
