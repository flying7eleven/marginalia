package com.marginalia.ui

import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
import javax.swing.SwingUtilities

class ChatPanel(
    private val onSend: (String) -> Unit,
    private val onGenerateNow: () -> Unit,
) : JPanel(BorderLayout()) {

    private val messagesPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }

    private val scrollableWrapper = object : JPanel(BorderLayout()), Scrollable {
        init { add(messagesPanel, BorderLayout.NORTH) }
        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
        override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 16
        override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = visibleRect.height
        override fun getScrollableTracksViewportWidth() = true
        override fun getScrollableTracksViewportHeight() = false
    }

    private val scrollPane = JBScrollPane(scrollableWrapper).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        border = JBUI.Borders.empty()
    }

    private val inputArea = JBTextArea(3, 0).apply {
        lineWrap = true
        wrapStyleWord = true
        border = JBUI.Borders.empty(4)
    }

    private val sendButton = JButton("Send")
    private val generateNowButton = JButton("Generate Now")

    private val thinkingLabel = JBLabel("Thinking...", AnimatedIcon.Default(), JBLabel.LEFT).apply {
        border = JBUI.Borders.empty(4, 8)
        isVisible = false
    }

    init {
        val inputScrollPane = JBScrollPane(inputArea).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            border = JBUI.Borders.empty()
        }

        val buttonPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyTop(4)
            add(generateNowButton, BorderLayout.WEST)
            add(sendButton, BorderLayout.EAST)
        }

        val inputPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(inputScrollPane, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }

        val bottomPanel = JPanel(BorderLayout()).apply {
            add(thinkingLabel, BorderLayout.NORTH)
            add(inputPanel, BorderLayout.CENTER)
        }

        add(scrollPane, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        sendButton.addActionListener { send() }
        generateNowButton.addActionListener { onGenerateNow() }

        inputArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER && !e.isShiftDown) {
                    e.consume()
                    send()
                }
            }
        })
    }

    fun addUserMessage(content: String) {
        addBubble(MessageBubble.Role.USER, content)
    }

    fun addAssistantMessage(content: String) {
        addBubble(MessageBubble.Role.ASSISTANT, content)
    }

    fun setThinking(thinking: Boolean) {
        thinkingLabel.isVisible = thinking
        sendButton.isEnabled = !thinking
        inputArea.isEnabled = !thinking
    }

    private fun addBubble(role: MessageBubble.Role, content: String) {
        messagesPanel.add(MessageBubble(role, content))
        messagesPanel.revalidate()
        messagesPanel.repaint()
        SwingUtilities.invokeLater {
            val bar = scrollPane.verticalScrollBar
            bar.value = bar.maximum
        }
    }

    private fun send() {
        val text = inputArea.text.trim()
        if (text.isEmpty()) return
        inputArea.text = ""
        onSend(text)
    }
}
