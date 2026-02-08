package rs2.interfaces

enum class InterfaceRenderer(val label: String) {
    CLASSIC("Oldschool"),
    NEW("Editor");

    override fun toString(): String = label
}