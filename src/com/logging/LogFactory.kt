package com.logging

import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.Logger

/**
 * Interface tool
 * paolo 04/08/2019
 * #Shnek6969
 */
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
