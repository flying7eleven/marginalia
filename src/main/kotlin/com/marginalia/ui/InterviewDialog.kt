package com.marginalia.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.marginalia.interview.InterviewEngine
import com.marginalia.interview.InterviewEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.nio.file.Path
import javax.swing.Action
import javax.swing.JComponent
import kotlin.io.path.writeText

class InterviewDialog(
    private val project: Project?,
    private val engine: InterviewEngine,
    private val specsDir: Path,
) : DialogWrapper(project) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val chatPanel = ChatPanel(
        onSend = { text -> engine.respond(text) },
        onGenerateNow = { engine.generateNow() },
    )

    init {
        title = "Marginalia \u2014 Project Interview"
        isModal = false
        init()
        subscribeToEvents()
        engine.start()
    }

    override fun createCenterPanel(): JComponent {
        chatPanel.minimumSize = Dimension(500, 400)
        chatPanel.preferredSize = Dimension(700, 550)
        return chatPanel
    }

    override fun createActions(): Array<Action> = arrayOf(cancelAction)

    override fun dispose() {
        scope.cancel()
        super.dispose()
    }

    private fun subscribeToEvents() {
        scope.launch {
            engine.events.collect { event ->
                ApplicationManager.getApplication().invokeLater {
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: InterviewEvent) {
        when (event) {
            is InterviewEvent.AssistantMessage -> {
                chatPanel.setThinking(false)
                chatPanel.addAssistantMessage(event.content)
            }
            is InterviewEvent.UserMessage -> {
                chatPanel.addUserMessage(event.content)
            }
            is InterviewEvent.Thinking -> {
                chatPanel.setThinking(true)
            }
            is InterviewEvent.Complete -> {
                chatPanel.setThinking(false)
                writeProductDescription(event.productDescription)
                close(OK_EXIT_CODE)
            }
            is InterviewEvent.Error -> {
                chatPanel.setThinking(false)
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Marginalia")
                    .createNotification(event.message, NotificationType.ERROR)
                    .notify(project)
            }
        }
    }

    private fun writeProductDescription(content: String) {
        val file = specsDir.resolve("product-description.md")
        file.writeText(content)
    }
}
