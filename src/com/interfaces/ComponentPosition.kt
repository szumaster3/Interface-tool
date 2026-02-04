package com.interfaces

import com.alex.utils.Constants
import com.interfaces.InterfaceUtils.getParent
import com.rs.cache.loaders.ComponentDefinition

object ComponentPosition {
    /**
     * sets the component on the right position
     * @param component
     * @param parentWidth
     * @param parentHeight
     */
    fun realPosition(component: ComponentDefinition, parentWidth: Int, parentHeight: Int) {
        /**
         * x Type
         */
        if (component.xMode.toInt() == 0) component.positionX = component.baseX
        else if (component.xMode.toInt() == 1) component.positionX =
            ((component.baseX + (parentWidth - component.width) / 2))
        else if (2 == component.xMode.toInt()) component.positionX = (parentWidth - component.width - component.baseX)
        else if (component.xMode.toInt() == 3) component.positionX = (parentWidth * (component.baseX) shr 14)
        else if (component.xMode.toInt() == 4) component.positionX =
            ((parentWidth - component.width) / 2 + (parentWidth * (component.baseX) shr 14))
        else component.positionX = (parentWidth - component.width - (component.baseX * parentWidth shr 14))
        /**
         * Y type
         */
        if (component.yMode.toInt() == 0) component.positionY = component.baseY
        else if (component.yMode.toInt() == 1) component.positionY =
            ((component.baseY + (parentHeight - component.height) / 2))
        else if (component.yMode.toInt() == 2) component.positionY = (parentHeight - component.height - component.baseY)
        else if (3 == component.yMode.toInt()) component.positionY = (component.baseY * parentHeight shr 14)
        else if (component.yMode.toInt() == 4) component.positionY =
            (((component.baseY * parentHeight shr 14) + (parentHeight - component.height) / 2))
        else component.positionY = (parentHeight - component.height - (component.baseY * parentHeight shr 14))

        /**
         * checking if values aren't to big
         *//*			if (component.positionX < 0)
				component.positionX = 0;
		    if ((component.positionX + component.width) > parentWidth)
				component.positionX = (parentWidth - component.width);
			if (component.positionY < 0)
				component.positionY = 0;
			 if ((component.positionY + component.height) > parentHeight)
				component.positionY = (parentHeight - component.height);*/
    }

    /**
     * gets the right scale of the component
     * @param component
     * @param width
     * @param height
     * @param bool
     * @param i_3_
     */
    fun realAfmeting(component: ComponentDefinition, width: Int, height: Int) {/* width */
        if (component.aspectWidthType.toInt() == 0) component.width = component.baseWidth
        else if (1 == component.aspectWidthType.toInt()) component.width = (width - component.baseWidth)
        else if (2 == component.aspectWidthType.toInt()) component.width = (width * (component.baseWidth) shr 14)
        //height
        if (0 == component.aspectHeightType.toInt()) component.height = component.baseHeight
        else if (1 == component.aspectHeightType.toInt()) component.height = (height - component.baseHeight)
        else if (component.aspectHeightType.toInt() == 2) component.height = (height * (component.baseHeight) shr 14)

        if (component.type == 0) {
            if (component.height < 5 && component.width < 5) {
                component.height = 5
                component.width = 5
            } else {
                if (component.height <= 0) component.height = 5
                if (component.width <= 0) component.width = 5
            }
        }


        /*
			if (component.aspectWidthType == 4)
				component.width = (component.anInt1158 * (component.height) / (component.anInt1242));
			if (component.aspectHeightType == 4)
				component.height = (component.width * (component.anInt1242) / (component.anInt1158));
			*/
    }

    @JvmStatic
    fun setValues(component: ComponentDefinition?) {
        if (component == null) return
        val parent = getParent(component.parentId)
        val width: Int
        val height: Int
        if (null == parent) {
            width = Constants.VIEWPORT_WIDTH
            height = Constants.VIEWPORT_HEIGHT
        } else {
            setValues(parent)
            width = parent.width
            height = parent.height
        }
        realAfmeting(component, width, height)
        realPosition(component, width, height)
    }
}
