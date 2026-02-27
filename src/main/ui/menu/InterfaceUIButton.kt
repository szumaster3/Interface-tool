package ui.menu

import fs.com.alex.Cache
import properties.PropertyValues
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import rs.cache.sprite.util.SpriteManager
import ui.Consts
import ui.InterfaceUI
import util.ColorPicker
import util.GenericPrinter
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.LineBorder

object InterfaceUIButton {
    fun addSprite() = InterfaceUI.instance?.addComponent(801, 4)
    fun addModel()  = InterfaceUI.instance?.addComponent(205, 90) { it.baseX = 0; it.baseY = 0 }
    fun addLayer()  = InterfaceUI.instance?.addComponent(778, 0) { it.baseX = 0; it.baseY = 0; it.baseWidth = 50; it.baseHeight = 50 }
    fun addFigure() = InterfaceUI.instance?.addComponent(625, 0) { it.baseX = 0; it.baseY = 0 }
    fun addText()   = InterfaceUI.instance?.addComponent(752, 5) { it.baseX = 0; it.baseY = 0 }
    fun addLine()   = InterfaceUI.instance?.addComponent(658, 39) { it.baseX = 0; it.baseY = 0 }

    fun addNewInterface() {
        val newId = ComponentDefinition.getInterfaceDefinitionsSize()
        val comp = ComponentDefinition().apply {
            componentId = newId
            type = ComponentType.SPRITE
            optionBase = ""
            baseX = 0
            baseY = 0
            baseWidth = 18
            baseHeight = 18
            spriteId = 18
            parentId = -1
        }
        Cache.getStore()!!.indexes[3].putFile(comp.componentId, 0, comp.encodeIf3())
        ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        InterfaceUI.instance?.interfacesListModel?.addElement(newId)
        InterfaceUI.instance?.interfacesList?.setSelectedValue(newId, true)
        InterfaceUI.instance?.interfacesList?.ensureIndexIsVisible(InterfaceUI.instance?.interfacesListModel!!.size - 1)
        InterfaceUI.instance?.renderPanel?.interfaceId = newId
        InterfaceUI.instance?.loadComponents(newId)
    }

    fun deleteComponent()
    {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component first.")
            return
        }

        if (comp.interfaceId <= 0 || comp.componentId <= 0) {
            JOptionPane.showMessageDialog(null, "Please select a valid component first.")
            return
        }

        val message = buildString {
            append("Are you sure that you want to remove component ${comp.componentId} from interface ${comp.interfaceId}?")
            if (comp.type == ComponentType.LAYER) {
                append(" NOTE: this component is a layer, childs will be removed aswell.")
            }
        }

        val confirm = JOptionPane.showConfirmDialog(
            null,
            message,
            "Warning",
            JOptionPane.YES_NO_OPTION
        )

        if (confirm != JOptionPane.YES_OPTION) return

        try {
            Cache.getStore()!!.indexes[3].removeFile(comp.interfaceId, comp.componentId)
            Cache.getStore()!!.indexes[3].resetCachedFiles()
            Cache.getStore()!!.indexes[3].rewriteTable()

            try {
                Cache.init(PropertyValues.cache_path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } finally {
            ComponentDefinition.getInterface(comp.interfaceId, true)
            InterfaceUI.instance?.renderPanel?.interfaceId = comp.interfaceId
            InterfaceUI.instance?.loadComponents(comp.interfaceId)
            InterfaceUI.instance?.renderPanel?.repaint()
        }
    }

    fun deleteInterface() {
        val interfaceId = InterfaceUI.instance?.interfacesList?.selectedValue ?: return
        val confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to clean interface $interfaceId?", "Warning", JOptionPane.YES_NO_OPTION)
        if (confirm != JOptionPane.YES_OPTION) return

        val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
        for (i in 0 until size) {
            if (i == 0) {
                val base = ComponentDefinition.getInterfaceComponent(interfaceId, 0)
                base?.parentId = -1
                Cache.getStore()!!.indexes[3].putFile(interfaceId, 0, base!!.encodeIf3())
            } else {
                Cache.getStore()!!.indexes[3].removeFile(interfaceId, i)
            }
        }
        try {
            Cache.init(PropertyValues.cache_path)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        InterfaceUI.instance?.let {
            it.loadComponents(interfaceId)
            it.renderPanel?.let { panel ->
                panel.interfaceId = interfaceId
                panel.repaint()
            }
        }
    }

    fun saveComponent() {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component before saving it.")
            return
        }
        if (comp.interfaceId == -1 || comp.componentId == -1) {
            JOptionPane.showMessageDialog(null, "Please select a valid component & interface before saving it.")
            return
        }
        InterfaceUI.instance?.save(comp)
        Cache.getStore()?.indexes?.get(3)?.putFile(comp.interfaceId, comp.componentId, comp.encode(true))
        InterfaceUI.instance?.loadComponents(comp.interfaceId)
        ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)?.let { InterfaceUI.instance?.selectComponent(it) }
        InterfaceUI.instance?.renderPanel?.interfaceId = comp.interfaceId
        InterfaceUI.instance?.renderPanel?.repaint()
    }

    fun copyComponent() {
        var comp = InterfaceUI.selectedComponent
        if (comp != null) {
            ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)
        }
    }

    fun pasteComponent() {
        var comp = InterfaceUI.selectedComponent
        if (comp == null) {
            JOptionPane.showMessageDialog(null, "No component was selected to paste.")
            return
        }
        if (comp.type == ComponentType.LAYER) {
            val layer = ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId)
            comp.parentId = -1

            Cache.getStore()!!.indexes[3].putFile(comp.interfaceId, layer, comp.encode(true))
            val childs = ComponentDefinition.getChildsByParent(comp.interfaceId, comp.componentHash)
            for (c in childs) {
                if (c.type != ComponentType.LAYER) {
                    Cache.getStore()!!.indexes[3].putFile(comp.interfaceId, ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp!!.interfaceId), c.encode(true))
                }
            }
            ComponentDefinition.getInterface(comp.interfaceId, true)
            val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId)
            val parent = ComponentDefinition.getInterfaceComponent(comp.interfaceId, layer)
            for (i in size - childs.size - 1 until size) {
                val component = ComponentDefinition.getInterfaceComponent(comp.interfaceId, i)
                if (component != null && component.type != 0) {
                    if (parent != null) {
                        component.parentId = parent.componentHash
                    }
                    Cache.getStore()!!.indexes[3].putFile(comp.interfaceId, i, component.encode(true))
                }
            }
        } else {
            comp.parentId = -1
            Cache.getStore()!!.indexes[3].putFile(comp.interfaceId, ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId), comp.encode(true))
        }
        ComponentDefinition.getInterface(comp.interfaceId, true)
        InterfaceUI.instance?.loadComponents(comp.interfaceId)
        ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)?.let { InterfaceUI.instance?.selectComponent(it) }
        InterfaceUI.instance?.renderPanel?.interfaceId = comp.interfaceId
        InterfaceUI.instance?.renderPanel?.repaint()
    }

    fun printComponent() {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component or interface.")
            return
        }
        val optionPanel = JPanel()
        val group = ButtonGroup()
        val compRadio = JRadioButton("Selected Component", true)
        val ifaceRadio = JRadioButton("Dump All")
        group.add(compRadio); group.add(ifaceRadio)
        optionPanel.add(compRadio); optionPanel.add(ifaceRadio)
        val result = JOptionPane.showConfirmDialog(null, optionPanel, "Select an option", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result != JOptionPane.OK_OPTION) return
        try {
            if (compRadio.isSelected) {
                GenericPrinter.printComponent(comp.interfaceId, comp.componentId)
                JOptionPane.showMessageDialog(null, "Component [${comp.componentId}] from interface [${comp.interfaceId}] was exported to data/prints/")
            } else {
                GenericPrinter.printInterface(comp.interfaceId)
                val count = ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId)
                JOptionPane.showMessageDialog(null, "Interface [${comp.interfaceId}] with $count components was exported to data/prints/")
            }
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(null, "Error during export: ${ex.message}")
            ex.printStackTrace()
        }
    }

    fun openSpriteLibrary() {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        SpriteManager(null) { spriteId, w, h, frameId ->
            if (comp.interfaceId <= 0) return@SpriteManager
            comp.spriteId = (spriteId ?: frameId) ?: return@SpriteManager
            comp.baseWidth = w
            comp.baseHeight = h
            InterfaceUI.instance?.buildPropertyPanel(comp)
            InterfaceUI.instance?.repaint()
        }
    }

    fun openColorPicker() {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        val colorField = InterfaceUI.instance?.fieldMap?.get("Color") as? JTextField ?: return
        val picker = ColorPicker(comp, colorField) {
            InterfaceUI.instance?.set("Color", comp.color)
            InterfaceUI.instance?.repaint()
        }
        picker.pack()
        picker.setLocationRelativeTo(null)
        picker.isVisible = true
    }

    fun aboutDialogue() {
        JDialog(null as JFrame?, "Credits", true).apply {
            layout = BorderLayout()
            val message = JLabel(
                "<html>" +
                        "<h2>Interface Editor</h2>" +
                        "<p>Made by: Shnek<br>Discord: Cara Shnek#6969</p>" +
                        "<p>Major code refactoring & improvements by: Ceikry</p>"
            )
            add(message, BorderLayout.CENTER)
            setSize(400, 200)
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    fun createButton(name: String, tooltip: String? = null, border: Border? = null, onClick: () -> Unit): JButton {
        return JButton(name).apply {
            tooltip?.let { toolTipText = it }
            border?.let { this.border = it }
            addActionListener { onClick() }
        }
    }

    fun newInterfaceButton()    = createButton(name = "New",    tooltip = "Create a new interface") { addNewInterface() }

    fun deleteButton()          = createButton(name = "Delete", onClick = { deleteComponent() })

    fun cleanInterfaceButton()  = createButton(name = "Clean",  tooltip = "Clean the selected interface (remove all components except 0)") { deleteInterface() }

    fun addSpriteButton()       = createButton(name = "Sprite", tooltip = "Create a sprite component", LineBorder(Consts.SPRITE_COLOR, 1)) { addSprite() }

    fun addModelButton()        = createButton(name = "Model",  tooltip = "Create a model component", LineBorder(Consts.MODEL_COLOR, 1)) { addModel() }

    fun addLayerButton()        = createButton(name = "Layer",  tooltip = "Create a layer component", LineBorder(Consts.LAYER_COLOR, 1)) { addLayer() }

    fun addFigureButton()       = createButton(name = "Figure", tooltip = "Create a figure component", LineBorder(Consts.FIGURE_COLOR, 1)) { addFigure() }

    fun addTextButton()         = createButton(name = "Text",   tooltip = "Create a text component", LineBorder(Consts.TEXT_COLOR, 1)) { addText() }

    fun addLineButton()         = createButton(name = "Line",   tooltip = "Create a line component", LineBorder(Consts.LINE_COLOR, 1)) { addLine() }

    fun saveButton()            = createButton(name = "Save",   tooltip = "Save the selected component") { saveComponent() }

    fun copyButton()            = createButton(name = "Copy",   tooltip = "Copy the selected component") { copyComponent() }

    fun pasteButton()           = createButton(name = "Paste",  tooltip = "Paste the copied component into the selected interface") { pasteComponent() }

    fun printButton()           = createButton(name = "dumptxt",   tooltip = "Print either the selected component or the entire interface to a text file") { printComponent() }

    fun spriteLibraryButton()   = createButton(name = "sprites", tooltip = "Allows you to select any sprite loaded from the cache") { openSpriteLibrary() }

    fun colorPickButton()       = createButton(name = "Pick color", tooltip = "Select the color") { openColorPicker() }

    fun aboutButton()           = createButton(name = "About",  tooltip = "Show credits") { aboutDialogue() }
}