package com.marginalia.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
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
                .align(AlignX.FILL)
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
                .rows(5)
                .align(AlignX.FILL)
                .align(AlignY.FILL)
                .applyToComponent { text = description }
                .onChanged { description = it.text }
                .comment("Brief description of the project (optional)")
        }.resizableRow()
    }

    companion object {
        val LANGUAGES = listOf("Kotlin", "Java", "Python", "TypeScript", "Go", "Rust", "Other")
    }
}
