package com.marginalia.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.marginalia.ai.ClaudeCliDiscovery
import javax.swing.JComponent

class MarginaliaSettingsConfigurable : Configurable {

    private val settings get() = MarginaliaSettings.getInstance()

    private var panel: com.intellij.openapi.ui.DialogPanel? = null

    override fun getDisplayName(): String = "Marginalia"

    override fun createComponent(): JComponent {
        val state = settings.state
        val detectedPath = ClaudeCliDiscovery.discover(state.claudeCliPath.ifBlank { null })

        val p = panel {
            group("Claude Code CLI") {
                row("CLI path:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                            .withTitle("Select Claude CLI Binary"),
                    )
                        .columns(COLUMNS_LARGE)
                        .bindText(state::claudeCliPath)
                        .comment(
                            if (detectedPath != null) "Detected: $detectedPath"
                            else "Leave empty for auto-detection. Install Claude Code from https://claude.ai/download"
                        )
                }
            }
            group("Interview") {
                row("Max questions:") {
                    spinner(3..15)
                        .bindIntValue(state::maxQuestions)
                        .comment("Number of questions the AI will ask (3\u201315)")
                }
                row {
                    checkBox("Automatically start interview after project creation")
                        .bindSelected(state::autoStartInterview)
                }
            }
        }

        panel = p
        return p
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
        ClaudeCliDiscovery.invalidateCache()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
