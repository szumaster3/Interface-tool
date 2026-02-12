package rs2.interfaces.draw

import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.InterfaceUtils.getParent
import kotlin.math.max

object OldschoolDrawPosition {

    private const val FIXED_SHIFT = 14

    private fun applySize(
        c: ComponentDefinition,
        parentWidth: Int,
        parentHeight: Int
    ) {
        c.width = when (c.aspectWidthType.toInt()) {
            0 -> c.baseWidth
            1 -> parentWidth - c.baseWidth
            2 -> parentWidth * c.baseWidth shr FIXED_SHIFT
            3 -> c.dynamicWidth
            else -> c.baseWidth
        }

        c.height = when (c.aspectHeightType.toInt()) {
            0 -> c.baseHeight
            1 -> parentHeight - c.baseHeight
            2 -> parentHeight * c.baseHeight shr FIXED_SHIFT
            3 -> c.dynamicHeight
            else -> c.baseHeight
        }

        c.width = max(1, c.width)
        c.height = max(1, c.height)
    }

    private fun applyPosition(
        c: ComponentDefinition,
        parentWidth: Int,
        parentHeight: Int
    ) {
        c.positionX = when (c.xMode.toInt()) {
            0 -> c.baseX
            1 -> (parentWidth - c.width) / 2 + c.baseX
            2 -> parentWidth - c.width - c.baseX
            3 -> parentWidth * c.baseX shr FIXED_SHIFT
            4 -> (parentWidth - c.width) / 2 +
                    (parentWidth * c.baseX shr FIXED_SHIFT)
            5 -> parentWidth - c.width -
                    (parentWidth * c.baseX shr FIXED_SHIFT)
            else -> c.baseX
        }

        c.positionY = when (c.yMode.toInt()) {
            0 -> c.baseY
            1 -> (parentHeight - c.height) / 2 + c.baseY
            2 -> parentHeight - c.height - c.baseY
            3 -> parentHeight * c.baseY shr FIXED_SHIFT
            4 -> (parentHeight - c.height) / 2 +
                    (parentHeight * c.baseY shr FIXED_SHIFT)
            5 -> parentHeight - c.height -
                    (parentHeight * c.baseY shr FIXED_SHIFT)
            else -> c.baseY
        }
    }

    @JvmStatic
    fun setValues(component: ComponentDefinition?) {
        if (component == null) return

        val parent = getParent(component.parentId)

        val parentWidth: Int
        val parentHeight: Int

        if (parent == null) {
            parentWidth = Constants.VIEWPORT_WIDTH
            parentHeight = Constants.VIEWPORT_HEIGHT
        } else {
            if (parent.width <= 0 || parent.height <= 0) {
                setValues(parent)
            }
            parentWidth = parent.width
            parentHeight = parent.height
        }

        applySize(component, parentWidth, parentHeight)
        applyPosition(component, parentWidth, parentHeight)
    }
}