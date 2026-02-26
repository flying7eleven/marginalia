package com.marginalia.wizard

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.rows
import com.marginalia.interview.InterviewLauncher
import com.marginalia.scaffold.ProjectConfig
import com.marginalia.scaffold.ScaffoldService
import java.nio.file.Path

class MarginaliaProjectWizardStep(parentStep: NewProjectWizardStep) : AbstractNewProjectWizardStep(parentStep) {

    val languageProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { LANGUAGES.first() }

    val descriptionProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { "" }

    var language: String by languageProperty
    var description: String by descriptionProperty

    override fun setupUI(builder: Panel) {
        builder.apply {
            row("Language:") {
                comboBox(LANGUAGES)
                    .bindItem(languageProperty)
            }
            row("Description:") {
                textArea()
                    .columns(COLUMNS_LARGE)
                    .rows(3)
                    .bindText(descriptionProperty)
                    .comment("Brief description of the project (optional)")
            }
        }
    }

    override fun setupProject(project: Project) {
        val baseData = with(NewProjectWizardBaseData) { this@MarginaliaProjectWizardStep.baseData }
            ?: error("NewProjectWizardBaseData not found in wizard chain")

        val config = ProjectConfig(
            name = baseData.name,
            rootDir = Path.of(baseData.path),
            language = language,
            description = description,
        )

        val scaffoldService = ApplicationManager.getApplication().getService(ScaffoldService::class.java)
        val result = try {
            scaffoldService.scaffold(config)
        } catch (e: Exception) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Marginalia")
                .createNotification(
                    "Marginalia",
                    "Project scaffolding failed: ${e.message}",
                    NotificationType.ERROR,
                )
                .notify(project)
            return
        }

        InterviewLauncher.launch(project, config, result.specsDir)
    }

    companion object {
        val LANGUAGES = listOf("Kotlin", "Java", "Python", "TypeScript", "Go", "Rust", "Other")
    }
}
