package rs2.editor.ui

import com.alex.loaders.interfaces.ComponentType
import java.awt.Color

object Consts {
    const val APP_VERSION = "1.0.11"

    const val EDITOR_WIDTH = 1024
    const val EDITOR_HEIGHT = 768

    const val SECTION_WIDTH = 250
    const val SECTION_HEIGHT = 239

    const val LEFT_PANEL_WIDTH = 190
    const val RIGHT_PANEL_WIDTH = 302

    const val CENTER_PANEL_WIDTH = 512
    const val CENTER_PANEL_HEIGHT = 334

    const val BUTTON_HEIGHT_SMALL = 25

    private const val ALPHA = 90

    val fillColors = mapOf(
        ComponentType.SPRITE to Color(90, 90, 220, ALPHA),
        ComponentType.FIGURE to Color(255, 160, 0, ALPHA),
        ComponentType.TEXT   to Color(255, 255, 0, 50),
        ComponentType.MODEL  to Color(230, 230, 230, ALPHA),
        ComponentType.LAYER  to Color(0, 250, 0, 50),
        ComponentType.LINE   to Color(255, 255, 255, ALPHA)
    )

    val outlineColors = mapOf(
        ComponentType.SPRITE to Color(90, 90, 220),
        ComponentType.FIGURE to Color(255, 145, 0),
        ComponentType.TEXT   to Color(255, 250, 0),
        ComponentType.MODEL  to Color(230, 230, 230),
        ComponentType.LAYER  to Color(0, 255, 0),
        ComponentType.LINE   to Color(255, 255, 255)
    )

    @JvmField val SPRITE_COLOR = outlineColors[ComponentType.SPRITE]
    @JvmField val FIGURE_COLOR = outlineColors[ComponentType.FIGURE]
    @JvmField val TEXT_COLOR   = outlineColors[ComponentType.TEXT]
    @JvmField val MODEL_COLOR  = outlineColors[ComponentType.MODEL]
    @JvmField val LAYER_COLOR  = outlineColors[ComponentType.LAYER]
    @JvmField val LINE_COLOR   = outlineColors[ComponentType.LINE]

}