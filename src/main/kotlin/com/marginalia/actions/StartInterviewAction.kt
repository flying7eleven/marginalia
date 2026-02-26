package com.marginalia.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.marginalia.ai.ClaudeCliClient
import com.marginalia.ai.ClaudeCliDiscovery
import com.marginalia.interview.InterviewEngine
import com.marginalia.scaffold.ProjectConfig
import com.marginalia.settings.MarginaliaSettings
import com.marginalia.ui.InterviewDialog
import java.nio.file.Files
import java.nio.file.Path

class StartInterviewAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val specsDir = detectSpecsDir(project)
        if (specsDir == null) {
            notify(project, "No specs directory found. Expected <project>-specs/ sibling or specs/ subdirectory.", NotificationType.WARNING)
            return
        }

        val settings = MarginaliaSettings.getInstance()
        val cliBinary = ClaudeCliDiscovery.discover(settings.state.claudeCliPath.ifBlank { null })
        if (cliBinary == null) {
            notify(project, "Claude Code CLI not found. Install Claude Code from https://claude.ai/download to enable the AI interview.", NotificationType.WARNING)
            return
        }

        val existingDescription = readExistingDescription(specsDir)

        val dialog = ProjectMetadataDialog(project, existingDescription)
        if (!dialog.showAndGet()) return

        val config = ProjectConfig(
            name = dialog.projectName,
            rootDir = Path.of(project.basePath!!),
            language = dialog.language,
            description = dialog.description,
        )

        val aiClient = ClaudeCliClient(cliBinary)
        val engine = InterviewEngine(aiClient, config)
        InterviewDialog(project, engine, specsDir).show()
    }

    private fun detectSpecsDir(project: Project): Path? {
        val basePath = project.basePath ?: return null
        val projectDir = Path.of(basePath)

        // 1. Sibling <project>-specs/ directory (Marginalia dual-repo convention)
        val siblingSpecs = projectDir.resolveSibling(projectDir.fileName.toString() + "-specs")
        if (Files.isDirectory(siblingSpecs)) return siblingSpecs

        // 2. specs/ subdirectory (single-repo fallback)
        val subSpecs = projectDir.resolve("specs")
        if (Files.isDirectory(subSpecs)) return subSpecs

        return null
    }

    private fun readExistingDescription(specsDir: Path): String {
        val file = specsDir.resolve("product-description.md")
        return if (Files.exists(file)) Files.readString(file) else ""
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Marginalia")
            .createNotification("Marginalia", message, type)
            .notify(project)
    }
}
