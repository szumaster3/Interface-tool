package com.interfaces

import com.rs.cache.loaders.ComponentDefinition

/**
 * Interface tool
 * paolo 04/08/2019
 * #Shnek6969
 */
object ContainerHelper {
    /**
     * checks if the component has the scrollbar script.
     */
    fun isScrollBar(component: ComponentDefinition?): Boolean {
        if (component?.onLoadScript == null || component.onLoadScript!!.size == 0) return false

        try {
            val first = component.onLoadScript!![0]
            return first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT || first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_2
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * checks if the component is a button.
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
