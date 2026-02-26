package com.marginalia.interview

import com.marginalia.ai.AiClient
import com.marginalia.scaffold.ProjectConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class InterviewEngineTest {

    private val config = ProjectConfig(
        name = "TestProject",
        rootDir = Path.of("/tmp/test"),
        language = "Kotlin",
        description = "A test project",
    )

    private fun fakeClient(vararg responses: String): FakeAiClient =
        FakeAiClient(responses.toMutableList())

    private fun engine(client: AiClient): InterviewEngine =
        InterviewEngine(client, config, Dispatchers.Unconfined)

    @Test
    fun `start emits Thinking then AssistantMessage`() = runTest {
        val client = fakeClient("What problem does your project solve?")
        val engine = engine(client)

        val events = mutableListOf<InterviewEvent>()
        val job = launch(Dispatchers.Unconfined) {
            engine.events.take(2).toList(events)
        }

        engine.start()
        job.join()

        assertEquals(2, events.size)
        assertInstanceOf(InterviewEvent.Thinking::class.java, events[0])
        assertInstanceOf(InterviewEvent.AssistantMessage::class.java, events[1])
        assertEquals("What problem does your project solve?", (events[1] as InterviewEvent.AssistantMessage).content)
    }

    @Test
    fun `start passes system prompt on first call`() = runTest {
        val client = fakeClient("First question?")
        val engine = engine(client)

        val events = mutableListOf<InterviewEvent>()
        val job = launch(Dispatchers.Unconfined) {
            engine.events.take(2).toList(events)
        }
        engine.start()
        job.join()

        assertNotNull(client.lastSystemPrompt, "System prompt should be set on first call")
        assertTrue(client.lastSystemPrompt!!.contains("TestProject"))
    }

    @Test
    fun `respond sends only the user message without system prompt`() = runTest {
        val client = fakeClient("First question?", "Follow-up question?")
        val engine = engine(client)

        // start the interview
        val startEvents = mutableListOf<InterviewEvent>()
        val startJob = launch(Dispatchers.Unconfined) { engine.events.take(2).toList(startEvents) }
        engine.start()
        startJob.join()

        // respond
        val respondEvents = mutableListOf<InterviewEvent>()
        val respondJob = launch(Dispatchers.Unconfined) { engine.events.take(3).toList(respondEvents) }
        engine.respond("My answer")
        respondJob.join()

        assertEquals(3, respondEvents.size)
        assertInstanceOf(InterviewEvent.UserMessage::class.java, respondEvents[0])
        assertEquals("My answer", (respondEvents[0] as InterviewEvent.UserMessage).content)
        assertInstanceOf(InterviewEvent.Thinking::class.java, respondEvents[1])
        assertInstanceOf(InterviewEvent.AssistantMessage::class.java, respondEvents[2])

        // System prompt should be null on second call (resume)
        assertNull(client.lastSystemPrompt, "System prompt should be null after first call")
        assertEquals("My answer", client.lastMessage)
    }

    @Test
    fun `generateNow emits Complete with extracted description`() = runTest {
        val description = "# TestProject — Product Description\n\n## What is TestProject?\nA great app."
        val response = "Here is the result:\n<product-description>\n$description\n</product-description>"
        val client = fakeClient("First question?", response)
        val engine = engine(client)

        // start
        val startEvents = mutableListOf<InterviewEvent>()
        val startJob = launch(Dispatchers.Unconfined) { engine.events.take(2).toList(startEvents) }
        engine.start()
        startJob.join()

        // generateNow — expect UserMessage, Thinking, AssistantMessage, Complete
        val genEvents = mutableListOf<InterviewEvent>()
        val genJob = launch(Dispatchers.Unconfined) { engine.events.take(4).toList(genEvents) }
        engine.generateNow()
        genJob.join()

        assertEquals(4, genEvents.size)
        assertInstanceOf(InterviewEvent.UserMessage::class.java, genEvents[0])
        assertInstanceOf(InterviewEvent.Thinking::class.java, genEvents[1])
        assertInstanceOf(InterviewEvent.AssistantMessage::class.java, genEvents[2])
        assertInstanceOf(InterviewEvent.Complete::class.java, genEvents[3])
        assertEquals(description, (genEvents[3] as InterviewEvent.Complete).productDescription)
    }

    @Test
    fun `extractProductDescription extracts content between tags`() {
        val text = "Some text\n<product-description>\n# My Project\nDetails here.\n</product-description>\nMore text"
        val result = InterviewEngine.extractProductDescription(text)
        assertEquals("# My Project\nDetails here.", result)
    }

    @Test
    fun `extractProductDescription returns null when no tags present`() {
        val result = InterviewEngine.extractProductDescription("Just a normal response with no tags.")
        assertNull(result)
    }

    @Test
    fun `error handling emits Error event when AI client throws`() = runTest {
        val client = object : AiClient {
            override suspend fun chat(systemPrompt: String?, message: String): String {
                throw RuntimeException("API connection failed")
            }
        }
        val engine = engine(client)

        val events = mutableListOf<InterviewEvent>()
        val job = launch(Dispatchers.Unconfined) {
            engine.events.take(2).toList(events)
        }
        engine.start()
        job.join()

        assertEquals(2, events.size)
        assertInstanceOf(InterviewEvent.Thinking::class.java, events[0])
        assertInstanceOf(InterviewEvent.Error::class.java, events[1])
        assertEquals("API connection failed", (events[1] as InterviewEvent.Error).message)
    }

    @Test
    fun `each call sends only the latest message, not full history`() = runTest {
        val client = fakeClient("Question 1?", "Question 2?", "Question 3?")
        val engine = engine(client)

        // start
        val e1 = mutableListOf<InterviewEvent>()
        val j1 = launch(Dispatchers.Unconfined) { engine.events.take(2).toList(e1) }
        engine.start()
        j1.join()

        // first respond
        val e2 = mutableListOf<InterviewEvent>()
        val j2 = launch(Dispatchers.Unconfined) { engine.events.take(3).toList(e2) }
        engine.respond("Answer 1")
        j2.join()
        assertEquals("Answer 1", client.lastMessage)

        // second respond — should only send the latest message
        val e3 = mutableListOf<InterviewEvent>()
        val j3 = launch(Dispatchers.Unconfined) { engine.events.take(3).toList(e3) }
        engine.respond("Answer 2")
        j3.join()
        assertEquals("Answer 2", client.lastMessage)
    }

    private class FakeAiClient(private val responses: MutableList<String>) : AiClient {
        var lastSystemPrompt: String? = null
        var lastMessage: String? = null

        override suspend fun chat(systemPrompt: String?, message: String): String {
            lastSystemPrompt = systemPrompt
            lastMessage = message
            return responses.removeFirst()
        }
    }
}
