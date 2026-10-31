package com.alex.loaders.interfaces

object ComponentType {
    const val LAYER: Int = 0
    const val MODEL_LIST: Int = 1
    const val INVENTORY: Int = 2
    const val FIGURE: Int = 3
    const val TEXT: Int = 4
    const val SPRITE: Int = 5
    const val MODEL: Int = 6
    const val INVENTORY_TEXT: Int = 7
    const val TOOLTIP: Int = 8
    const val LINE: Int = 9

    /**
     * Names of all component types, in the order they are listed in the editor.
     */
    val names: Map<Int, String> = linkedMapOf(
        LAYER to "layer",
        MODEL_LIST to "model list",
        INVENTORY to "inventory",
        FIGURE to "figure",
        TEXT to "text",
        SPRITE to "sprite",
        MODEL to "model",
        INVENTORY_TEXT to "inventory text",
        TOOLTIP to "tooltip",
        LINE to "line"
    )

    /**
     * Types which only exist in the old IF1 interface format.
     */
    val if1Only: Set<Int> = setOf(MODEL_LIST, INVENTORY, INVENTORY_TEXT, TOOLTIP)

    fun nameOf(type: Int): String = names[type] ?: type.toString()

    /**
     * @return the type with the given name, the number itself when it is numeric, or -1.
     */
    fun fromName(name: String?): Int =
        name?.let { n -> names.entries.firstOrNull { it.value == n }?.key ?: n.toIntOrNull() } ?: -1
}
