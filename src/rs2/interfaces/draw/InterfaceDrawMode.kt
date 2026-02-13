package rs2.interfaces.draw

enum class InterfaceDrawMode(
    val label: String,
) {
    EDITING("Edit"),
    PREVIEW("Preview"),
    ;

    override fun toString(): String = label
}
