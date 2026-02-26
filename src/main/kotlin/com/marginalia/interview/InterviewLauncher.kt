package com.marginalia.interview

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.marginalia.ai.AnthropicAiClient
import com.marginalia.scaffold.ProjectConfig
import com.marginalia.settings.MarginaliaSettings
import com.marginalia.ui.InterviewDialog
import java.nio.file.Path

object InterviewLauncher {

    fun launch(project: Project?, config: ProjectConfig, specsDir: Path) {
        val settings = MarginaliaSettings.getInstance()

        if (!settings.state.autoStartInterview) return

        val apiKey = settings.apiKey
        if (apiKey.isNullOrBlank()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Marginalia")
                .createNotification(
                    "Marginalia",
                    "No API key configured. Set your Anthropic API key in Settings > Tools > Marginalia to enable the AI interview.",
                    NotificationType.WARNING,
                )
                .notify(project)
            return
        }

        val aiClient = AnthropicAiClient(apiKey, settings.state.modelName)
        val engine = InterviewEngine(aiClient, config)
        val dialog = InterviewDialog(project, engine, specsDir)
        dialog.show()
    }
}
