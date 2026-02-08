package rs2.logging

import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.Logger

object LogFactory {
    @JvmStatic
    fun createLogger(name: String?): Logger {
        val logger = Logger.getLogger(name)
        logger.useParentHandlers = false
        val handler = ConsoleHandler()
        val formatter: Formatter = LogFormatter()
        handler.formatter = formatter
        logger.addHandler(handler)
        return logger
    }
}
