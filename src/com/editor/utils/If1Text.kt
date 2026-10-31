package com.editor.utils

/**
 * Text form of the IF1 values which are lists, so they can be edited in a single text field.
 *
 * - comparisons: `opcode:operand;opcode:operand`
 * - scripts: numbers separated by `,`, scripts separated by `|`
 * - inventory slots: `slot:x,y,sprite;slot:x,y,sprite`
 */
object If1Text {

    fun formatComparisons(opcodes: IntArray?, operands: IntArray?): String {
        if (opcodes == null || operands == null) return ""
        return opcodes.indices.joinToString(";") { "${opcodes[it]}:${operands[it]}" }
    }

    /** @return the opcodes and the operands, or null when the text is empty. */
    fun parseComparisons(text: String): Pair<IntArray, IntArray>? {
        val pairs = text.split(';').map { it.trim() }.filter { it.isNotEmpty() }.mapNotNull { part ->
            val values = part.split(':')
            val opcode = values.getOrNull(0)?.trim()?.toIntOrNull()
            val operand = values.getOrNull(1)?.trim()?.toIntOrNull()
            if (opcode != null && operand != null) opcode to operand else null
        }
        if (pairs.isEmpty()) return null
        return IntArray(pairs.size) { pairs[it].first } to IntArray(pairs.size) { pairs[it].second }
    }

    fun formatScripts(scripts: Array<IntArray>?): String =
        scripts?.joinToString("|") { script -> script.joinToString(",") } ?: ""

    fun parseScripts(text: String): Array<IntArray>? {
        if (text.isBlank()) return null
        return text.split('|').map { script ->
            script.split(',').mapNotNull { it.trim().toIntOrNull() }.toIntArray()
        }.toTypedArray()
    }

    class Slot(val index: Int, val x: Int, val y: Int, val sprite: Int)

    fun formatSlots(present: BooleanArray, x: IntArray?, y: IntArray?, sprite: IntArray?): String {
        if (x == null || y == null || sprite == null) return ""
        return present.indices.filter { present[it] }.joinToString(";") { "$it:${x[it]},${y[it]},${sprite[it]}" }
    }

    fun parseSlots(text: String): List<Slot> =
        text.split(';').map { it.trim() }.filter { it.isNotEmpty() }.mapNotNull { part ->
            val index = part.substringBefore(':', "").trim().toIntOrNull()
            val values = part.substringAfter(':', "").split(',').map { it.trim().toIntOrNull() }
            if (index != null && index in 0..19 && values.size == 3 && values.all { it != null }) {
                Slot(index, values[0]!!, values[1]!!, values[2]!!)
            } else {
                null
            }
        }
}
