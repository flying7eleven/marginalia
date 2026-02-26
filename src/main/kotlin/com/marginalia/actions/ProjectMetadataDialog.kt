package com.marginalia.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.rows
import javax.swing.JComponent

class ProjectMetadataDialog(
    private val project: Project,
    existingDescription: String,
) : DialogWrapper(project) {

    var projectName: String = project.name
    var language: String = LANGUAGES.first()
    var description: String = existingDescription

    init {
        title = "Marginalia \u2014 Project Metadata"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Project name:") {
            textField()
                .columns(COLUMNS_LARGE)
                .applyToComponent { text = projectName }
                .onChanged { projectName = it.text }
        }
        row("Language:") {
            comboBox(LANGUAGES)
                .applyToComponent { selectedItem = language }
                .onChanged { language = it.selectedItem as? String ?: LANGUAGES.first() }
        }
        row("Description:") {
            textArea()
                .columns(COLUMNS_LARGE)
                .rows(3)
                .applyToComponent { text = description }
                .onChanged { description = it.text }
                .comment("Brief description of the project (optional)")
        }
    }

    companion object {
        val LANGUAGES = listOf("Kotlin", "Java", "Python", "TypeScript", "Go", "Rust", "Other")
    }
}
