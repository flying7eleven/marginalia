package com.marginalia.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class MarginaliaSettingsConfigurable : Configurable {

    private val settings get() = MarginaliaSettings.getInstance()

    private val apiKeyField = JBPasswordField()
    private var panel: com.intellij.openapi.ui.DialogPanel? = null

    override fun getDisplayName(): String = "Marginalia"

    override fun createComponent(): JComponent {
        val state = settings.state

        val p = panel {
            group("API Configuration") {
                row("API key:") {
                    cell(apiKeyField)
                        .comment("Your Anthropic API key (stored securely in OS keychain)")
                        .onApply { settings.apiKey = String(apiKeyField.password).ifBlank { null } }
                        .onReset { apiKeyField.text = settings.apiKey.orEmpty() }
                        .onIsModified { String(apiKeyField.password) != settings.apiKey.orEmpty() }
                }
                row("Model:") {
                    textField()
                        .bindText(state::modelName)
                        .comment("Anthropic model ID (e.g. claude-sonnet-4-20250514)")
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
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
