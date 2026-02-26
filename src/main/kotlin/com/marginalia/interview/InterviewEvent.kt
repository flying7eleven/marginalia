package com.marginalia.interview

sealed class InterviewEvent {
    data class AssistantMessage(val content: String) : InterviewEvent()
    data class UserMessage(val content: String) : InterviewEvent()
    data object Thinking : InterviewEvent()
    data class Complete(val productDescription: String) : InterviewEvent()
    data class Error(val message: String) : InterviewEvent()
}
