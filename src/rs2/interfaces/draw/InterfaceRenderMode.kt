package rs2.interfaces.draw

enum class InterfaceRenderMode(
    val label: String,
) {
    CLASSIC("Edit"),
    NEW("Preview"),
    ;

    override fun toString(): String = label
}
