package rs2.interfaces

import rs2.cache.loaders.ComponentDefinition

object ContainerHelper {
    @JvmStatic
    fun isScrollBar(component: ComponentDefinition?): Boolean {
        if (component?.onLoadScript == null || component.onLoadScript!!.size == 0) return false

        try {
            val first = component.onLoadScript!![0]
            return first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT ||
                first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_2 ||
                first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_3 ||
                first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_4 ||
                first.toString().toInt() == ComponentConstants.SCROLL_BAR_SCRIPT_5
        } catch (e: Exception) {
            return false
        }
    }

    @JvmStatic
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
