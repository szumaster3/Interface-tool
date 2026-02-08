package rs2.interfaces.draw

import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.InterfaceUtils

object ComponentPosition2 {

    fun realPosition(component: ComponentDefinition, parentWidth: Int, parentHeight: Int) {

        component.positionX =
            when (component.xMode) {
                (0).toByte() -> component.baseX
                (1).toByte() -> component.baseX + (parentWidth - component.width) / 2
                (2).toByte() -> parentWidth - component.width - component.baseX
                (3).toByte() -> parentWidth * component.baseX shr 14
                (4).toByte() -> (parentWidth - component.width) / 2 + (parentWidth * component.baseX shr 14)
                else -> parentWidth - component.width - (component.baseX * parentWidth shr 14)
            }

        component.positionY =
            when (component.yMode) {
                (0).toByte() -> component.baseY
                (1).toByte() -> component.baseY + (parentHeight - component.height) / 2
                (2).toByte() -> parentHeight - component.height - component.baseY
                (3).toByte() -> component.baseY * parentHeight shr 14
                (4).toByte() -> (component.baseY * parentHeight shr 14) + (parentHeight - component.height) / 2
                else -> parentHeight - component.height - (component.baseY * parentHeight shr 14)
            }
    }

    fun realAfmeting(component: ComponentDefinition, width: Int, height: Int) {

        component.width =
            when (component.aspectWidthType) {
                (0).toByte() -> component.baseWidth
                (1).toByte() -> width - component.baseWidth
                (2).toByte() -> width * component.baseWidth shr 14
                else -> component.width
            }

        component.height =
            when (component.aspectHeightType) {
                (0).toByte() -> component.baseHeight
                (1).toByte() -> height - component.baseHeight
                (2).toByte() -> height * component.baseHeight shr 14
                else -> component.height
            }

        if (component.type == 0) {
            if (component.width < 5 || component.height < 5) {
                component.width = maxOf(component.width, 5)
                component.height = maxOf(component.height, 5)
            }
        }
    }

    fun setValues(component: ComponentDefinition?) {
        if (component == null) return

        val parent = InterfaceUtils.getParent(component.parentId)

        val (width, height) =
            if (parent == null) {
                Constants.VIEWPORT_WIDTH to Constants.VIEWPORT_HEIGHT
            } else {
                setValues(parent)
                parent.width to parent.height
            }

        realAfmeting(component, width, height)
        realPosition(component, width, height)
    }
}
