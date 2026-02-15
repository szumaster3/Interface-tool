package rs2.interfaces

import rs2.cache.loaders.ComponentDefinition

object ContainerHelper {
    private val SCROLL_BAR_SCRIPTS =
        setOf(
            ComponentConstants.SCROLL_BAR_SCRIPT,
            ComponentConstants.SCROLL_BAR_SCRIPT_2,
            ComponentConstants.SCROLL_BAR_SCRIPT_3,
            ComponentConstants.SCROLL_BAR_SCRIPT_4,
            ComponentConstants.SCROLL_BAR_SCRIPT_5,
        )

    @JvmStatic
    fun isScrollBar(component: ComponentDefinition?): Boolean {
        if (component?.baseWidth ?: 0 > 16) return false

        val first =
            component
                ?.onLoadScript
                ?.firstOrNull()
                ?.toString()
                ?.toIntOrNull()
                ?: return false

        return first in SCROLL_BAR_SCRIPTS
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
