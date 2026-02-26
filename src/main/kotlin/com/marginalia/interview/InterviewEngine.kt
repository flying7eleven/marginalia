package com.marginalia.interview

import com.marginalia.ai.AiClient
import com.marginalia.ai.ChatMessage
import com.marginalia.ai.Role
import com.marginalia.scaffold.ProjectConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class InterviewEngine(
    private val aiClient: AiClient,
    private val projectConfig: ProjectConfig,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<InterviewEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<InterviewEvent> = _events.asSharedFlow()

    private val history = mutableListOf<ChatMessage>()

    private val systemPrompt: String = """
        |You are a product analyst helping a developer define their new software project.
        |
        |The project is called "${projectConfig.name}" and is described as: "${projectConfig.description}"
        |The primary language is ${projectConfig.language}.
        |
        |Your job is to conduct a brief interview (5–8 questions) to produce a clear, useful
        |product description document. Cover these topics across your questions:
        |- Target users / personas
        |- Core problem being solved
        |- Key features and capabilities
        |- Success criteria (measurable)
        |- Constraints (technical, business, or design)
        |
        |Ask one question at a time. Keep questions concise and focused. Build on previous
        |answers rather than repeating information the user already provided.
        |
        |When you have enough information (or the user asks you to finish), produce the final
        |product description wrapped in <product-description> tags. The content inside the tags
        |should be Markdown, following this structure:
        |
        |# ${projectConfig.name} — Product Description
        |
        |## What is ${projectConfig.name}?
        |## Target Users
        |## Core Problem
        |## Key Features
        |## Success Criteria
        |## Constraints
        |
        |Do NOT include the tags in any response until you are ready to deliver the final document.
    """.trimMargin()

    fun start() {
        scope.launch {
            chat()
        }
    }

    fun respond(userInput: String) {
        history.add(ChatMessage(Role.USER, userInput))
        _events.tryEmit(InterviewEvent.UserMessage(userInput))
        scope.launch {
            chat()
        }
    }

    fun generateNow() {
        val prompt = "Please generate the product description now based on what we've discussed so far."
        history.add(ChatMessage(Role.USER, prompt))
        _events.tryEmit(InterviewEvent.UserMessage(prompt))
        scope.launch {
            chat()
        }
    }

    private suspend fun chat() {
        _events.tryEmit(InterviewEvent.Thinking)
        try {
            val response = aiClient.chat(systemPrompt, history)
            history.add(ChatMessage(Role.ASSISTANT, response))

            val description = extractProductDescription(response)
            if (description != null) {
                _events.tryEmit(InterviewEvent.AssistantMessage(response))
                _events.tryEmit(InterviewEvent.Complete(description))
            } else {
                _events.tryEmit(InterviewEvent.AssistantMessage(response))
            }
        } catch (e: Exception) {
            _events.tryEmit(InterviewEvent.Error(e.message ?: "Unknown error"))
        }
    }

    companion object {
        private val TAG_REGEX =
            Regex("<product-description>(.*?)</product-description>", RegexOption.DOT_MATCHES_ALL)

        internal fun extractProductDescription(text: String): String? =
            TAG_REGEX.find(text)?.groupValues?.get(1)?.trim()
    }
}
