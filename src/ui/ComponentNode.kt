package ui

data class ComponentNode(
    val componentId: Int,
    val type: String
) {
    override fun toString(): String =
        "$componentId: ($type)"
}