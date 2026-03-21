package rs2.editor.ui.utils

import com.alex.loaders.interfaces.ComponentDefinition

data class ComponentCopy(
    val component: ComponentDefinition,
    val children: List<ComponentCopy>
)