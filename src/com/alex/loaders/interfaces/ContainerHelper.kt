package com.alex.loaders.interfaces

/**
 * @author paolo #Shnek6969
 * @since 04/08/2019
 */
object ContainerHelper {
    fun isScrollBar(component: ComponentDefinition): Boolean {
        return component.onLoadScript != null && component.onLoadScript!![0].toString()
            .toInt() == 30 && component.baseWidth < 32
    }

    fun isButton(component: ComponentDefinition): Boolean {
        return component.onLoadScript != null && component.onLoadScript!![0].toString().toInt() == 92
    }
}