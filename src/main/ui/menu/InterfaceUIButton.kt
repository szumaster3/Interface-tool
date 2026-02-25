package ui.menu

import fs.com.alex.Cache
import properties.PropertyValues
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import rs.cache.sprite.util.SpriteManager
import ui.Consts
import ui.InterfaceUI
import ui.draw.InterfaceDraw
import util.ColorPicker
import util.GenericPrinter
import util.InterfaceUtils
import javax.swing.*
import javax.swing.border.LineBorder
import java.awt.BorderLayout

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
        val comp = InterfaceUI.instance?.selectedComponent ?: run {
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
        val comp = InterfaceUI.instance?.selectedComponent ?: run {
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
        val comp = InterfaceUI.instance?.selectedComponent ?: return
        if (comp.interfaceId <= 0) return
        InterfaceUI.instance?.clonedComponent = ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)
    }

    fun pasteComponent() {
        val i = InterfaceUI.instance!!.selectedComponent ?: return
        if (i.interfaceId == -1) {
            return
        }
        InterfaceUtils.pasteComponent()

        ComponentDefinition.getInterface(i.interfaceId, true)
        InterfaceUI.instance?.loadComponents(i.interfaceId)

        val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(i.interfaceId)
        val newComp = ComponentDefinition.getInterfaceComponent(i.interfaceId, size - 1)
        if (newComp != null) InterfaceUI.instance?.selectComponent(newComp)

        InterfaceUI.instance?.repaint()
    }

    fun printComponent() {
        val comp = InterfaceUI.instance?.selectedComponent ?: run {
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
        val comp = InterfaceUI.instance?.selectedComponent ?: run {
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
        val comp = InterfaceUI.instance?.selectedComponent ?: run {
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
                        "<p>Major code refactoring & improvements by: Ceikry</p>" +
                        "<p>Recent changes: szumaster (05.02.2026)</p></html>"
            )
            add(message, BorderLayout.CENTER)
            setSize(400, 200)
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    fun newInterfaceButton(): JButton = JButton("New").apply {
        toolTipText = "Create a new interface."
        addActionListener { addNewInterface() }
    }
    fun deleteButton(): JButton = JButton("<html><center>Delete<br>component</html>").apply {
        font = font.deriveFont(9f)
        addActionListener { deleteComponent() }
    }
    fun cleanInterfaceButton(): JButton = JButton("Clean").apply {
        toolTipText = "Clean the selected interface (remove all components except 0)."
        addActionListener { deleteInterface() }
    }

    fun addSpriteButton() = JButton("Sprite").apply {
        border = LineBorder(Consts.SPRITE_COLOR, 1)
        toolTipText = "Create a sprite component."
        addActionListener { addSprite() }
    }
    fun addModelButton()  = JButton("Model").apply  {
        border = LineBorder(Consts.MODEL_COLOR, 1)
        toolTipText = "Create a model component."
        addActionListener { addModel() }
    }
    fun addLayerButton()  = JButton("Layer").apply  {
        border = LineBorder(Consts.LAYER_COLOR, 1)
        toolTipText = "Create a layer component."
        addActionListener { addLayer() }
    }
    fun addFigureButton() = JButton("Figure").apply {
        border = LineBorder(Consts.FIGURE_COLOR, 1)
        toolTipText = "Create a figure component."
        addActionListener { addFigure() }
    }
    fun addTextButton()   = JButton("Text").apply   {
        border = LineBorder(Consts.TEXT_COLOR, 1)
        toolTipText = "Create a text component."
        addActionListener { addText() }
    }
    fun addLineButton()   = JButton("Line").apply   {
        border = LineBorder(Consts.LINE_COLOR, 1)
        toolTipText = "Create a line component."
        addActionListener { addLine() }
    }

    fun saveButton() = JButton("Save").apply {
        toolTipText = "Save the selected component."
        addActionListener { saveComponent() }
    }
    fun copyButton() = JButton("Copy").apply {
        toolTipText = "Copy the selected component."
        addActionListener { copyComponent() }
    }
    fun pasteButton() = JButton("Paste").apply {
        toolTipText = "Paste the copied component into the selected interface."
        addActionListener { pasteComponent() }
    }

    fun printButton() = JButton("<html><center>Dump<br>to txt file</html>").apply {
        font = font.deriveFont(9f)
        toolTipText = "Print either the selected component or the entire interface to a text file."
        addActionListener { printComponent() }
    }
    fun spriteLibraryButton() = JButton("<html><center>Sprite<br>Library</html>").apply {
        font = font.deriveFont(9f)
        toolTipText = "Allows you to select any sprite loaded from the cache."
        addActionListener { openSpriteLibrary() }
    }
    fun colorPickButton() = JButton("Picker").apply {
        toolTipText = "Select the color."
        addActionListener { openColorPicker() }
    }

    fun aboutButton() = JButton("About").apply {
        toolTipText = "Show credits."
        addActionListener { aboutDialogue() }
    }
}