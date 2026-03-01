package ui.selection

import rs.cache.ComponentDefinition

object SelectionModel {

    var selected: ComponentDefinition? = null
        set(value) {
            field = value
            listeners.forEach { it(value) }
        }

    private val listeners = mutableListOf<(ComponentDefinition?) -> Unit>()

    fun addListener(listener: (ComponentDefinition?) -> Unit) {
        listeners += listener
    }


}