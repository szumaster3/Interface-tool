package com.editor.utils

import com.editor.properties.PropertyValues
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Names for the components of the old IF1 format, which has no field for a name. The names are not part of the
 * cache, they are kept in the file component_names.properties next to the configuration of the editor.
 */
object ComponentNames {

    private val names = Properties()
    private var loaded = false

    private fun file() = File(PropertyValues.getConfigDirectory(), "component_names.properties")

    private fun key(interfaceId: Int, componentId: Int) = "$interfaceId:$componentId"

    @Synchronized
    private fun load() {
        if (loaded) return
        loaded = true
        try {
            val file = file()
            if (file.exists()) FileInputStream(file).use { names.load(it) }
        } catch (e: Exception) {
            println("Could not read the component names: $e")
        }
    }

    @Synchronized
    private fun save() {
        try {
            FileOutputStream(file()).use { names.store(it, "Names of the components of IF1 interfaces: interface:component") }
        } catch (e: Exception) {
            println("Could not save the component names: $e")
        }
    }

    @Synchronized
    fun get(interfaceId: Int, componentId: Int): String? {
        load()
        return names.getProperty(key(interfaceId, componentId))?.takeIf { it.isNotEmpty() }
    }

    @Synchronized
    fun set(interfaceId: Int, componentId: Int, name: String?) {
        load()
        if (name.isNullOrEmpty()) names.remove(key(interfaceId, componentId)) else names.setProperty(key(interfaceId, componentId), name)
        save()
    }

    /**
     * The names of the components of an interface are used for another interface too.
     */
    @Synchronized
    fun copyInterface(from: Int, to: Int) {
        load()
        names.stringPropertyNames().filter { it.startsWith("$from:") }.forEach {
            names.setProperty("$to:${it.substringAfter(':')}", names.getProperty(it))
        }
        save()
    }

    @Synchronized
    fun removeInterface(interfaceId: Int) {
        load()
        names.stringPropertyNames().filter { it.startsWith("$interfaceId:") }.forEach { names.remove(it) }
        save()
    }

    /**
     * Two components exchange their ids, so they exchange their names.
     */
    @Synchronized
    fun swap(interfaceId: Int, first: Int, second: Int) {
        load()
        val a = names.getProperty(key(interfaceId, first))
        val b = names.getProperty(key(interfaceId, second))
        if (b == null) names.remove(key(interfaceId, first)) else names.setProperty(key(interfaceId, first), b)
        if (a == null) names.remove(key(interfaceId, second)) else names.setProperty(key(interfaceId, second), a)
        save()
    }
}
