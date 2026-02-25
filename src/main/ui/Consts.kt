package ui

import rs.cache.ComponentType
import java.awt.Color

object Consts {
    const val APP_VERSION = "1.0.3"

    const val EDITOR_WIDTH = 1440 - 253
    const val EDITOR_HEIGHT = 900 - 178

    const val SECTION_WIDTH = 265
    const val SECTION_HEIGHT = 250 + 22

    const val LEFT_PANEL_WIDTH = 256
    const val RIGHT_PANEL_WIDTH = 258 + 158 - 17

    const val CENTER_PANEL_WIDTH = 512
    const val CENTER_PANEL_HEIGHT = 334

    const val BUTTON_HEIGHT_SMALL = 25
    const val BUTTON_HEIGHT_MEDIUM = BUTTON_HEIGHT_SMALL + 5

    private const val ALPHA = 50

    val fillColors = mapOf(
        ComponentType.SPRITE to Color(90, 90, 220, ALPHA),
        ComponentType.FIGURE to Color(255, 160, 0, ALPHA),
        ComponentType.TEXT   to Color(255, 255, 0, ALPHA),
        ComponentType.MODEL  to Color(230, 230, 230, ALPHA),
        ComponentType.LAYER  to Color(0, 250, 0, ALPHA),
        ComponentType.LINE   to Color(255, 255, 255, ALPHA)
    )

    val outlineColors = mapOf(
        ComponentType.SPRITE to Color(90, 90, 220),
        ComponentType.FIGURE to Color(255, 160, 0),
        ComponentType.TEXT   to Color(255, 255, 0),
        ComponentType.MODEL  to Color(230, 230, 230),
        ComponentType.LAYER  to Color(0, 250, 0),
        ComponentType.LINE   to Color(255, 255, 255)
    )

    val SPRITE_COLOR = outlineColors[ComponentType.SPRITE]
    val FIGURE_COLOR = outlineColors[ComponentType.FIGURE]
    val TEXT_COLOR   = outlineColors[ComponentType.TEXT]
    val MODEL_COLOR  = outlineColors[ComponentType.MODEL]
    val LAYER_COLOR  = outlineColors[ComponentType.LAYER]
    val LINE_COLOR   = outlineColors[ComponentType.LINE]
}