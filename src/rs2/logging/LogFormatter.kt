package rs2.logging

import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

class LogFormatter : Formatter() {

    override fun format(record: LogRecord): String {

        val builder = StringBuilder()
        builder.append(ANSI_YELLOW)

        builder.append("[")
        builder.append(calcDate(record.millis))
        builder.append("]")

        builder.append(" [")
        builder.append(record.sourceClassName)
        builder.append("]")

        builder.append(" [")
        builder.append(getTypeColor(record.level.name))
        builder.append(record.level.name)
        builder.append(ANSI_YELLOW)
        builder.append("]")
        builder.append(ANSI_WHITE)
        builder.append(" - ")
        builder.append(record.message)

        val params = record.parameters

        if (params != null) {
            builder.append("\t")
            for (i in params.indices) {
                builder.append(params[i])
                if (i < params.size - 1) builder.append(", ")
            }
        }

        builder.append(ANSI_RESET)
        builder.append("\n")
        return builder.toString()
    }

    private fun getTypeColor(name: String): String {
        when (name) {
            "SEVERE" -> return ANSI_RED
            "INFO" -> return ANSI_GREEN
            "WARNING" -> return ANSI_YELLOW
        }
        return ANSI_WHITE
    }

    private fun calcDate(millisecs: Long): String {
        val date_format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val resultdate = Date(millisecs)
        return date_format.format(resultdate)
    }

    companion object {

        const val ANSI_RESET: String = "\u001B[0m"
        const val ANSI_BLACK: String = "\u001B[30m"
        const val ANSI_RED: String = "\u001B[31m"
        const val ANSI_GREEN: String = "\u001B[32m"
        const val ANSI_YELLOW: String = "\u001B[33m"
        const val ANSI_BLUE: String = "\u001B[34m"
        const val ANSI_PURPLE: String = "\u001B[35m"
        const val ANSI_CYAN: String = "\u001B[36m"
        const val ANSI_WHITE: String = "\u001B[37m"
    }
}
