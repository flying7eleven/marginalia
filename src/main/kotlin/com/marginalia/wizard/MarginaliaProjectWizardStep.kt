package com.marginalia.wizard

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.validation.CHECK_DIRECTORY
import com.intellij.openapi.ui.validation.CHECK_NON_EMPTY
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.rows
import com.intellij.ui.dsl.builder.textValidation
import com.intellij.ui.dsl.builder.trimmedTextValidation
import com.marginalia.interview.InterviewLauncher
import com.marginalia.scaffold.ProjectConfig
import com.marginalia.scaffold.ScaffoldService
import java.nio.file.Path

class MarginaliaProjectWizardStep(parentStep: NewProjectWizardStep) : AbstractNewProjectWizardStep(parentStep) {

    val nameProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { "" }

    val pathProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { suggestProjectDir() }

    val languageProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { LANGUAGES.first() }

    val descriptionProperty: GraphProperty<String> =
        propertyGraph.lazyProperty { "" }

    var name: String by nameProperty
    var path: String by pathProperty
    var language: String by languageProperty
    var description: String by descriptionProperty

    override fun setupUI(builder: Panel) {
        builder.apply {
            row("Project name:") {
                textField()
                    .columns(COLUMNS_LARGE)
                    .bindText(nameProperty)
                    .trimmedTextValidation(CHECK_NON_EMPTY)
            }
            row("Location:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    context.project,
                )
                    .columns(COLUMNS_LARGE)
                    .bindText(pathProperty)
                    .textValidation(CHECK_DIRECTORY)
            }
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
        val config = buildProjectConfig()

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

    fun buildProjectConfig(): ProjectConfig = ProjectConfig(
        name = name,
        rootDir = Path.of(path),
        language = language,
        description = description,
    )

    private fun suggestProjectDir(): String =
        System.getProperty("user.home") + "/IdeaProjects"

    companion object {
        val LANGUAGES = listOf("Kotlin", "Java", "Python", "TypeScript", "Go", "Rust", "Other")
    }
}
