package com.editor.utils

import com.alex.Cache
import com.alex.loaders.interfaces.ComponentDefinition
import com.editor.EditorGUI

/**
 * Changes the order of the components of a layer. The client draws the components of a layer in the order of
 * their ids, so a component is moved by exchanging its id with the one of its neighbour.
 *
 * Scripts which refer to a component by its id are not updated.
 */
object ComponentOrder {

    /**
     * Moves the component one place up (drawn earlier, behind its neighbour).
     */
    fun moveUp(component: ComponentDefinition) = move(component, -1)

    /**
     * Moves the component one place down (drawn later, in front of its neighbour).
     */
    fun moveDown(component: ComponentDefinition) = move(component, 1)

    /**
     * @return true when the component was moved.
     */
    private fun move(component: ComponentDefinition, direction: Int): Boolean {
        val index = Cache.store?.indexes?.getOrNull(Cache.interfaceIndex) ?: return false
        val interfaceId = component.interfaceId
        val all = ComponentDefinition.getInterface(interfaceId, false)?.filterNotNull() ?: return false

        val siblings = all.filter { it.parentId == component.parentId }.sortedBy { it.componentId }
        val position = siblings.indexOfFirst { it.componentId == component.componentId }
        val neighbour = siblings.getOrNull(position + direction) ?: return false

        val first = component
        val second = neighbour
        val firstId = first.componentId
        val secondId = second.componentId
        val firstHash = (interfaceId shl 16) + firstId
        val secondHash = (interfaceId shl 16) + secondId

        // the children follow their parents to the new ids
        for (other in all) {
            if (other === first || other === second) continue
            val parent = when (other.parentId) {
                firstHash -> secondHash
                secondHash -> firstHash
                else -> continue
            }
            other.parentId = parent
            index.putFile(interfaceId, other.componentId, other.encode())
        }
        // the data of the two components is written to the id of the other one
        val firstData = first.encode()
        val secondData = second.encode()
        index.putFile(interfaceId, secondId, firstData)
        index.putFile(interfaceId, firstId, secondData)
        ComponentNames.swap(interfaceId, firstId, secondId)

        ComponentDefinition.getInterface(interfaceId, true)
        EditorGUI.instance?.let { ui ->
            ui.loadComponents(interfaceId)
            ComponentDefinition.getInterfaceComponent(interfaceId, secondId)?.let { ui.selectComponent(it) }
        }
        return true
    }
}
