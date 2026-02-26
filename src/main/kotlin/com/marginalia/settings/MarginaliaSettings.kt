package com.marginalia.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "MarginaliaSettings", storages = [Storage("marginalia.xml")])
class MarginaliaSettings : PersistentStateComponent<MarginaliaSettings.State> {

    data class State(
        var modelName: String = "claude-sonnet-4-20250514",
        var maxQuestions: Int = 8,
        var autoStartInterview: Boolean = true,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var apiKey: String?
        get() = PasswordSafe.instance.getPassword(credentialAttributes)
        set(value) = PasswordSafe.instance.setPassword(credentialAttributes, value)

    companion object {
        private val credentialAttributes = CredentialAttributes(
            generateServiceName("Marginalia", "api-key"),
        )

        fun getInstance(): MarginaliaSettings =
            ApplicationManager.getApplication().getService(MarginaliaSettings::class.java)
    }
}
