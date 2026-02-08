package tools.print

import rs2.cache.loaders.ComponentDefinition
import rs2.cache.loaders.IComponentSettings
import java.io.FileWriter
import java.lang.reflect.Field
import java.util.logging.Logger

object GenericPrinter {
    fun printComponent(interfaceId: Int, componentId: Int) {
        try {
            FileWriter("component_" + interfaceId + "_" + componentId + ".txt").use { out ->
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

    @JvmStatic
    fun printInterface(interfaceId: Int) {
        try {
            FileWriter("interface_$interfaceId.txt").use { out ->
                val comps = ComponentDefinition.getInterface(interfaceId, true)
                if (comps == null) {
                    println("Interface not found: $interfaceId")
                    return
                }
                for (i in comps.indices) {
                    if (comps[i] != null) {
                        printComponent(comps[i], out)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun printComponent(comp: ComponentDefinition?, out: FileWriter) {
        var c: Class<*>? = comp!!.javaClass
        while (c != null) {
            for (f in c.declaredFields) {
                f.isAccessible = true
                val value = f[comp]
                if (shouldPrint(f, value)) {
                    out.write("comp." + f.name + " = " + format(value) + ";\n")
                }
            }
            c = c.superclass
        }
        printScripts(comp, out)

        out.write("\n\n")
        out.flush()
        println(">>> Copied component: " + comp.interfaceId + ":" + comp.componentId)
    }

    private fun shouldPrint(f: Field, v: Any?): Boolean {
        if (v == null) return false


        if (f.name == "componentDefinition") return false
        val type = f.type
        if (type.isArray && ComponentDefinition::class.java.isAssignableFrom(type.componentType)) return false
        if (v is Array<*> && v.isNotEmpty() && v[0] is ComponentDefinition) return false

        if (Logger::class.java.isAssignableFrom(type)) return false
        if (IComponentSettings::class.java.isAssignableFrom(type)) return true

        if (type == String::class.java) return (v as String).trim().isNotEmpty()
        if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) return v as Boolean
        if (Number::class.java.isAssignableFrom(type) || type.isPrimitive) {
            val `val` = (v as Number).toDouble()
            return `val` != 0.0 && `val` != -1.0
        }


        if (type.isArray) {
            val compType = type.componentType
            if (compType.isPrimitive || compType == String::class.java) {
                return java.lang.reflect.Array.getLength(v) > 0
            }
        }

        return false
    }

    private fun escapeUnicode(s: String): String {
        val sb = StringBuilder()
        for (c in s.toCharArray()) {
            if (c.code >= 32 && c.code <= 126) sb.append(c)
            else sb.append(String.format("\\u%04X", c.code))
        }
        return sb.toString()
    }

    private fun format(v: Any?): String {
        if (v == null) return "null"

        if (v is IComponentSettings) {
            val s = v
            return "new IComponentSettings(" + s.settings + ", " + s.mask + ")"
        }

        if (v is String) return "\"" + escapeUnicode(v) + "\""

        if (v.javaClass.isArray) {
            val len = java.lang.reflect.Array.getLength(v)
            val sb = StringBuilder("new ").append(v.javaClass.componentType.simpleName).append("[]{")
            for (i in 0 until len) {
                sb.append(format(java.lang.reflect.Array.get(v, i)))
                if (i < len - 1) sb.append(", ")
            }
            return sb.append("}").toString()
        }

        return v.toString()
    }

    private fun getType(type: Int): String {
        return when (type) {
            6 -> "MODEL"
            3 -> "FIGURE"
            4 -> "TEXT"
            0 -> "CONTAINER"
            5 -> "SPRITE"
            9 -> "UNKNOWN"
            else -> "TYPE_$type"
        }
    }

    @Throws(Exception::class)
    private fun printScripts(comp: ComponentDefinition?, out: FileWriter) {
        val fields = comp!!.javaClass.declaredFields
        for (f in fields) {
            f.isAccessible = true
            val value = f[comp]


            if (f.name == "componentDefinition") continue
            if (value is Array<*> && value.isNotEmpty() && value[0] is ComponentDefinition) continue


            if (value is Array<*>) {
                if (value.isNotEmpty() && value.all { it is ComponentDefinition }) continue
                val arr = value
                if (arr.size > 0) {
                    out.write("comp." + f.name + " = new Object[]{" + formatArray(arr) + "};\n")
                }
            } else if (value is IntArray) {
                val arr = value
                if (arr.size > 0) {
                    out.write("comp." + f.name + " = new int[]{" + formatIntArray(arr) + "};\n")
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun formatArray(arr: Array<*>): String {
        val sb = StringBuilder()
        for (i in arr.indices) {
            val v = arr[i]
            if (v is ComponentDefinition) continue
            sb.append(format(v))
            if (i < arr.size - 1) sb.append(", ")
        }
        return sb.toString()
    }
    private fun formatIntArray(arr: IntArray): String {
        val sb = StringBuilder()
        for (i in arr.indices) {
            sb.append(arr[i])
            if (i < arr.size - 1) sb.append(", ")
        }
        return sb.toString()
    }
}
