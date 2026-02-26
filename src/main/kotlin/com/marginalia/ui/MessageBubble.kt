package com.marginalia.ui

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel

class MessageBubble(
    private val role: Role,
    private val content: String,
) : JPanel(BorderLayout()) {

    enum class Role { USER, ASSISTANT }

    init {
        isOpaque = false
        border = JBUI.Borders.empty(4, 8)

        val roleLabel = JLabel(if (role == Role.USER) "You" else "Assistant").apply {
            font = font.deriveFont(java.awt.Font.BOLD, font.size2D - 1f)
            foreground = JBColor.GRAY
            border = JBUI.Borders.emptyBottom(2)
        }

        val textPane = JEditorPane().apply {
            contentType = "text/html"
            isEditable = false
            isOpaque = true
            background = if (role == Role.USER) USER_BG else ASSISTANT_BG
            border = JBUI.Borders.empty(8)
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            font = JBUI.Fonts.label()
            text = toHtml(content)
        }

        add(roleLabel, BorderLayout.NORTH)
        add(textPane, BorderLayout.CENTER)
    }

    companion object {
        private val USER_BG = JBColor(0xE3F2FD, 0x2B3C4D)
        private val ASSISTANT_BG = JBColor(0xF5F5F5, 0x3C3F41)

        internal fun toHtml(text: String): String {
            val escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")

            val formatted = escaped
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")
                .replace("\n\n", "<br><br>")
                .replace("\n", "<br>")

            return "<html><body style='margin:0;padding:0'>$formatted</body></html>"
        }
    }
}
