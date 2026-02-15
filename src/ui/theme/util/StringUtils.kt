package ui.theme.util

import javax.swing.JTextField

object StringUtils {
    fun setText(
        field: Any,
        value: Any?,
    ) {
        when (field) {
            is JTextField -> {
                field.text = value?.toString() ?: "null"
            }

            else -> {
                val method = field::class.java.methods.find { it.name == "getTextField" }
                val textField = method?.invoke(field) as? JTextField
                textField?.text = value?.toString() ?: "null"
            }
        }
    }

    fun setScript(
        field: JTextField,
        script: Array<Any?>?,
    ) {
        field.text = script?.joinToString(";") ?: ""
    }

    fun setTriggers(
        field: JTextField,
        triggers: IntArray?,
    ) {
        field.text = triggers?.joinToString("") ?: ""
    }
}
