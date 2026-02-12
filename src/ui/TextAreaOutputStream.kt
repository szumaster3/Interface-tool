package ui

import java.io.IOException
import java.io.OutputStream
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class TextAreaOutputStream(
    private val textArea: JTextArea,
    private val title: String,
) : OutputStream() {
    private val sb = StringBuilder()

    init {
        sb.append("$title> ")
    }

    override fun flush() {}

    override fun close() {}

    @Throws(IOException::class)
    override fun write(b: Int) {
        if (b == '\r'.code) return

        if (b == '\n'.code) {
            val text = sb.toString() + "\n"
            SwingUtilities.invokeLater { textArea.append(text) }
            sb.setLength(0)
            sb.append("$title> ")
            return
        }

        sb.append(b.toChar())
    }
}
