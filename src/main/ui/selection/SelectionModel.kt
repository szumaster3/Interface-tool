package ui.selection

import rs.cache.ComponentDefinition

object SelectionModel {

    var selected: ComponentDefinition? = null
        private set

    var source: SelectionSource = SelectionSource.DEFAULT
        private set

    private val listeners = mutableListOf<(ComponentDefinition?) -> Unit>()

    fun setSelection(comp: ComponentDefinition?, selectionSource: SelectionSource) {
        if (selected == comp) return

        selected = comp
        source = selectionSource
        listeners.forEach { it(comp) }
    }

    fun addListener(listener: (ComponentDefinition?) -> Unit) {
        listeners += listener
    }
}