package com.interfaces

import com.rs.cache.loaders.ComponentDefinition

/**
 * Interface tool
 * @author paolo
 * @date 04/08/2019
 * @discord #Shnek6969
 */
object ContainerHelper {
    /**
     * Checks if the component has the scrollbar script.
     */
    fun isScrollBar(component: ComponentDefinition?): Boolean {
        if (component?.onLoadScript == null || component.onLoadScript!!.size == 0) return false

        try {
            val first = component.onLoadScript!![0]
            return first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT ||
                    first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_2
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Checks if the component is a button.
     */
    fun isButton(component: ComponentDefinition?): Boolean {
        if (component?.onLoadScript == null || component.onLoadScript!!.size == 0) return false

        try {
            val first = component.onLoadScript!![0]
            return first.toString().toInt() == ComponentConstants.BUTTON_SCRIPT
        } catch (e: Exception) {
            return false
        }
    }
}
