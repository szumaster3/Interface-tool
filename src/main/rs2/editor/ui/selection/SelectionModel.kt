package rs2.editor.ui.selection

import com.alex.loaders.interfaces.ComponentDefinition

object SelectionModel {

    private var selected: ComponentDefinition? = null

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