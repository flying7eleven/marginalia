package com.marginalia.interview

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.marginalia.ai.ClaudeCliClient
import com.marginalia.ai.ClaudeCliDiscovery
import com.marginalia.scaffold.ProjectConfig
import com.marginalia.settings.MarginaliaSettings
import com.marginalia.ui.InterviewDialog
import java.nio.file.Path

object InterviewLauncher {

    fun launch(project: Project?, config: ProjectConfig, specsDir: Path) {
        val settings = MarginaliaSettings.getInstance()

        if (!settings.state.autoStartInterview) return

        val cliBinary = ClaudeCliDiscovery.discover(settings.state.claudeCliPath.ifBlank { null })
        if (cliBinary == null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Marginalia")
                .createNotification(
                    "Marginalia",
                    "Claude Code CLI not found. Install Claude Code from https://claude.ai/download to enable the AI interview.",
                    NotificationType.WARNING,
                )
                .notify(project)
            return
        }

        val aiClient = ClaudeCliClient(cliBinary)
        val engine = InterviewEngine(aiClient, config)
        val dialog = InterviewDialog(project, engine, specsDir)
        dialog.show()
    }
}
