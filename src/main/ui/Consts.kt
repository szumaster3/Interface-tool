package ui

import rs.cache.ComponentType
import java.awt.Color

object Consts {
    const val APP_VERSION = "1.0.0"

    const val EDITOR_WIDTH = 1440
    const val EDITOR_HEIGHT = 900

    const val SECTION_WIDTH = 256
    const val SECTION_HEIGHT = 250 + 26

    const val LEFT_PANEL_WIDTH = 256
    const val RIGHT_PANEL_WIDTH = 258 + 158 - 16

    const val CENTER_PANEL_WIDTH = 765
    const val CENTER_PANEL_HEIGHT = 503

    const val BUTTON_HEIGHT = 25

    const val ALPHA = 70

    val fillColors = mapOf(
        ComponentType.SPRITE to Color(80, 80, 200, Consts.ALPHA),
        ComponentType.FIGURE to Color(250, 160, 10, Consts.ALPHA),
        ComponentType.TEXT   to Color(255, 255, 0, Consts.ALPHA),
        ComponentType.MODEL  to Color(220, 220, 220, Consts.ALPHA),
        ComponentType.LAYER  to Color(0, 200, 0, Consts.ALPHA),
        ComponentType.LINE   to Color(255, 255, 255, Consts.ALPHA)
    )

    val outlineColors = mapOf(
        ComponentType.SPRITE to Color(80, 80, 200),
        ComponentType.FIGURE to Color(250, 160, 10),
        ComponentType.TEXT   to Color(255, 255, 0),
        ComponentType.MODEL  to Color(220, 220, 220),
        ComponentType.LAYER  to Color(0, 200, 0),
        ComponentType.LINE   to Color(255, 255, 255)
    )

    val SPRITE_COLOR = Color(80, 80, 200, ALPHA)
    val LAYER_COLOR  = Color(0, 200, 0, ALPHA)
    val FIGURE_COLOR = Color(250, 160, 10, ALPHA)
    val TEXT_COLOR   = Color(255, 255, 0, ALPHA)
    val MODEL_COLOR  = Color(220, 220, 220, ALPHA)
    val LINE_COLOR   = Color(255, 255, 255, ALPHA)
}