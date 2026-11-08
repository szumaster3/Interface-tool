package com.editor

/**
 * The areas of the game screen an interface is shown in: the sizes decide how the interface is laid out.
 */
enum class ScreenMode(val label: String, val width: Int, val height: Int) {
    TAB("Tab (190 x 261)", 190, 261),
    FIXED("Fixed (512 x 334)", 512, 334),
    FULL_SCREEN("Full screen (765 x 503)", 765, 503)
}
