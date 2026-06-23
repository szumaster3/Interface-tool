package com.editor.utils

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.IComponentSettings
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field

object EditorGenericTextPrinter {
    private val outputDir = File("data/prints/").apply {
        if (!exists()) mkdirs()
    }

    fun printComponent(interfaceId: Int, componentId: Int)
    {
        try {
            val dir = File(outputDir, interfaceId.toString())
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "component_$componentId.txt")
            FileWriter(file).use { out ->
                val comps = ComponentDefinition.getInterface(interfaceId, true)
                if (comps == null || componentId >= comps.size || comps[componentId] == null) {
                    println("Component not found: $interfaceId:$componentId")
                    return
                }
                val comp = comps[componentId]
                printComponent(comp, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printInterface(interfaceId: Int)
    {
        try {
            val file = File(outputDir, "interface_$interfaceId.txt")
            FileWriter(file).use { out ->
                val comps = ComponentDefinition.getInterface(interfaceId, true)
                if (comps == null) {
                    println("Interface not found: $interfaceId")
                    return
                }
                for (comp in comps) {
                    if (comp != null) printComponent(comp, out)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun printComponent(comp: ComponentDefinition?, out: FileWriter)
    {
        var clazz: Class<*>? = comp!!.javaClass
        while (clazz != null) {
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                val value = field[comp]
                if (shouldPrint(field, value)) {
                    out.write("comp.${field.name} = ${format(value)};\n")
                }
            }
            clazz = clazz.superclass
        }

        printScripts(comp, out)

        out.write("\n\n")
        out.flush()
        println(">>> Copied component: ${comp.interfaceId}:${comp.componentId}")
    }

    private fun shouldPrint(field: Field, value: Any?): Boolean
    {
        if (value == null) return false

        val name = field.name

        if (name == "componentDefinition" || name == "Companion") return false
        if (name.startsWith("hash")) return false
        if (name == "storeIndex") return false
        if (name == "logger") return false

        val type = field.type

        if (name.startsWith("on") || name == "rightClickOptions") return false

        if (type == String::class.java) return (value as String).trim().isNotEmpty()
        if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) return value as Boolean
        if (Number::class.java.isAssignableFrom(type) || type.isPrimitive) {
            val num = (value as Number).toDouble()
            return num != 0.0 && num != -1.0
        }

        if (type.isArray) {
            return when (value) {
                is Array<*>  -> value.isNotEmpty()
                is IntArray  -> value.any { it != 0 && it != -1 }
                is ByteArray -> value.any { it.toInt() != 0 }
                else -> true
            }
        }

        if (IComponentSettings::class.java.isAssignableFrom(type)) return true

        return true
    }

    private fun format(value: Any?): String
    {
        if (value == null) return "null"

        return when (value) {
            is String    -> "\"${escapeUnicode(value)}\""
            is IntArray  -> value.joinToString(prefix = "new int[]{", postfix = "}", separator = ", ")
            is ByteArray -> value.joinToString(prefix = "new byte[]{", postfix = "}", separator = ", ")
            is Array<*>  -> value.joinToString(prefix = "new Object[]{", postfix = "}", separator = ", ") { format(it) }
            is IComponentSettings -> "new IComponentSettings(${value.settings}, ${value.mask})"
            else -> value.toString()
        }
    }

    private fun escapeUnicode(s: String): String
    {
        val sb = StringBuilder()
        for (c in s) {
            sb.append(if (c.code in 32..126) c else "\\u%04X".format(c.code))
        }
        return sb.toString()
    }

    private fun printScripts(comp: ComponentDefinition?, out: FileWriter)
    {
        val fields = comp!!.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field[comp] ?: continue
            val name = field.name

            if (name.startsWith("on") || name == "rightClickOptions") {
                out.write("comp.$name = ${format(value)};\n")
            }
        }
    }

}
