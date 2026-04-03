package rs2.editor.ui.menu

import com.alex.Cache
import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.sprites.util.SpriteManager
import com.alex.util.crc32.CRC32HGenerator
import rs2.editor.properties.PropertyValues
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.render.InterfaceDraw
import rs2.editor.ui.util.ColorPicker
import rs2.editor.ui.util.GenericPrinter
import rs2.editor.ui.util.MultiCopy
import rs2.editor.ui.utils.ComponentCopy
import rs2.editor.ui.utils.InterfaceUtils.iconButton
import rs2.editor.ui.utils.InterfaceUtils.loadIcon
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*

object InterfaceUIButton {
    private lateinit var mouseButton: JButton
    lateinit var mouseMenuItem: JMenuItem
    private var copiedComponent: ComponentCopy? = null
    private val hashMap = mutableMapOf<Int, Int>()

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

    fun importInterface(): Array<ComponentDefinition?>?
    {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Select .dat file to import"
        val result = fileChooser.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) return null
        val file = fileChooser.selectedFile

        var interfaceId: Int? = null
        val nameMatch = Regex("interface_(\\d+)\\.dat").find(file.name)
        if (nameMatch != null) {
            interfaceId = nameMatch.groupValues[1].toIntOrNull()
        }

        while (interfaceId == null) {
            val input = JOptionPane.showInputDialog(
                null,
                "Enter interface ID for this .dat file:",
                "Interface ID Required",
                JOptionPane.QUESTION_MESSAGE
            ) ?: return null
            interfaceId = input.toIntOrNull()
            if (interfaceId == null) {
                JOptionPane.showMessageDialog(null, "Invalid ID. Please enter a number.")
            }
        }

        val confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to import interface $interfaceId from ${file.name}?",
            "Confirm Import",
            JOptionPane.YES_NO_OPTION
        )
        if (confirm != JOptionPane.YES_OPTION) return null

        val data = FileInputStream(file).use { it.readBytes() }
        val components: Array<ComponentDefinition?> =
            arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId))
        val stream = InputStream(data)

        var offset = 0
        while (offset < data.size) {
            val comp = ComponentDefinition()
            comp.decodeIf3(stream, offset, interfaceId)
            components[offset] = comp
            offset++
        }

        if (ComponentDefinition.componentDefinition == null) {
            ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        }
        ComponentDefinition.componentDefinition!![interfaceId] = components

        for ((componentId, comp) in components.withIndex()) {
            if (comp != null) {
                val bytes = comp.encode(comp.hasScripts)
                Cache.getStore().indexes[3].putFile(interfaceId, componentId, bytes)
            }
        }

        InterfaceUI.instance?.renderPanel?.interfaceId = interfaceId
        InterfaceUI.instance?.loadComponents(interfaceId)
        InterfaceUI.instance?.renderPanel?.repaint()

        JOptionPane.showMessageDialog(null, "Interface $interfaceId imported from ${file.name}")
        return components
    }

    fun exportInterface()
    {
        val interfaceId = InterfaceUI.selectedComponent!!.interfaceId
        val components = ComponentDefinition.getInterface(interfaceId, false) ?: return

        val confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to export interface $interfaceId to .dat?",
            "Confirm Export",
            JOptionPane.YES_NO_OPTION
        )

        if (confirm != JOptionPane.YES_OPTION) return

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Select output file for interface $interfaceId"
        fileChooser.selectedFile = File("interface_$interfaceId.dat")
        val result = fileChooser.showSaveDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) return

        val outputFile = fileChooser.selectedFile

        val outStream = OutputStream()
        for (c in components) {
            if (c != null) {
                val bytes = c.encode(c.hasScripts)
                outStream.writeBytes(bytes)
            }
        }

        val totalBytes = ByteArray(outStream.offset)
        outStream.offset = 0
        outStream.getBytes(totalBytes, 0, totalBytes.size)

        FileOutputStream(outputFile).use { fos ->
            fos.write(totalBytes)
        }

        JOptionPane.showMessageDialog(null, "Interface $interfaceId exported to ${outputFile.absolutePath}")
    }

    private fun deleteComponent()
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
                append(" NOTE: this component is a layer, childs will be removed as well.")
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

    private fun deleteInterface() {
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
            it.renderPanel.let { panel ->
                panel.interfaceId = interfaceId
                panel.repaint()
            }
        }
    }

    private fun saveComponent() {
        val comp = InterfaceUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component before saving it.")
            return
        }
        if (comp.interfaceId == -1 || comp.componentId == -1) {
            JOptionPane.showMessageDialog(null, "Please select a valid component & interface before saving it.")
            return
        }
        val store = Cache.getStore() ?: run {
            JOptionPane.showMessageDialog(null, "Cache not loaded.")
            return
        }

        InterfaceUI.instance?.save(comp)
        var fileNameHash: Int? = null

        comp.name?.takeIf { it.isNotBlank() }?.let {
            fileNameHash = CRC32HGenerator.getHash(
                it.uppercase(Locale.getDefault()).toByteArray(Charsets.UTF_8)
            )
        }

        val hashToUse = fileNameHash

        if (hashToUse != null) {
            store.indexes[3].putFile(
                comp.interfaceId,
                comp.componentId,
                2,
                comp.encode(true),
                null,
                true,
                true,
                -1,
                hashToUse
            )
        } else {
            store.indexes[3].putFile(
                comp.interfaceId,
                comp.componentId,
                comp.encode(true)
            )
        }
        InterfaceUI.instance?.loadComponents(comp.interfaceId)
        ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)?.let { InterfaceUI.instance?.selectComponent(it) }
        InterfaceUI.instance?.renderPanel?.interfaceId = comp.interfaceId
        InterfaceUI.instance?.renderPanel?.repaint()
    }

    fun buildCopyTree(comp: ComponentDefinition): ComponentCopy {
        val children = InterfaceDraw.childrenCache[comp.componentHash]
            ?.map { buildCopyTree(it) }
            ?: emptyList()

        return ComponentCopy(ComponentDefinition.copy(comp), children)
    }

    fun copyComponent() {
        val comp = InterfaceUI.selectedComponent ?: return
        copiedComponent = buildCopyTree(comp)
    }

    private fun pasteComponent() {
        val root = copiedComponent ?: return

        val targetInterfaceId = InterfaceUI.instance!!.interfacesList.selectedValue
            ?: return

        hashMap.clear()

        val newRoot = pasteRecursive(root, targetInterfaceId, null)
        ComponentDefinition.getInterface(targetInterfaceId, true)

        InterfaceUI.instance?.apply {
            loadComponents(targetInterfaceId)

            ComponentDefinition.getInterfaceComponent(
                targetInterfaceId,
                newRoot.componentId
            )?.let {
                selectComponent(it)
            }

            renderPanel?.interfaceId = targetInterfaceId
            renderPanel?.repaint()
        }
    }

    private fun pasteRecursive(
        node: ComponentCopy,
        targetInterfaceId: Int,
        newParentHash: Int?
    ): ComponentDefinition {

        val nextId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(targetInterfaceId)

        val newComp = ComponentDefinition.copy(node.component)


        newComp.interfaceId = targetInterfaceId
        newComp.componentHash = (targetInterfaceId shl 16) or nextId

        newComp.parentId = newParentHash ?: -1
        Cache.getStore()!!.indexes[3].putFile(
            targetInterfaceId,
            nextId,
            newComp.encode(true)
        )
        hashMap[node.component.componentHash] = newComp.componentHash
        node.children.forEach {
            pasteRecursive(it, targetInterfaceId, newComp.componentHash)
        }
        return newComp
    }

    private fun printComponent() {
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
        val comp = InterfaceUI.selectedComponent
        if (comp?.interfaceId == -1) {
            JOptionPane.showMessageDialog(null, "Please select a component first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        SpriteManager(null) { spriteId, w, h, _ ->
            comp!!.spriteId = spriteId
            comp.baseWidth = w
            comp.baseHeight = h
            InterfaceDraw.preparedRoots.forEach { root ->
                InterfaceDraw.layoutComponent(root, InterfaceUI.instance!!.renderPanel.width, InterfaceUI.instance!!.renderPanel.height)
            }
            InterfaceUI.instance?.renderPanel?.repaint()
            InterfaceUI.instance?.buildPropertyPanel(comp)
        }
    }

    private fun openColorPicker() {
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
        val dialog = JDialog(null as JFrame?, "Credits", true).apply {
            layout = BorderLayout(10, 10)

            val contentPanel = JPanel()
            contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
            contentPanel.border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

            val titleLabel = JLabel("Interface Editor")
            titleLabel.alignmentX = Component.CENTER_ALIGNMENT

            val authorLabel = JLabel("<html>Made by: Shnek<br>Discord: Cara Shnek#6969</html>")
            authorLabel.alignmentX = Component.CENTER_ALIGNMENT

            val refactorLabel = JLabel("<html>Major code refactoring & improvements by: Ceikry</html>")
            refactorLabel.alignmentX = Component.CENTER_ALIGNMENT

            contentPanel.add(titleLabel)
            contentPanel.add(Box.createVerticalStrut(10))
            contentPanel.add(authorLabel)
            contentPanel.add(Box.createVerticalStrut(5))
            contentPanel.add(refactorLabel)
            add(contentPanel, BorderLayout.CENTER)
            setSize(300, 150)
            setLocationRelativeTo(null)
            isResizable = false
            isVisible = true
        }
    }

    fun toggleMouseAction() {
        val panel = InterfaceUI.instance?.renderPanel ?: return

        panel.setInteractionEnabled(!panel.interactionEnabled)

        if (::mouseButton.isInitialized) {
            val iconOn = loadIcon("/icons/mouse_on.png")
            val iconOff = loadIcon("/icons/mouse_off.png")
            mouseButton.icon = if (panel.interactionEnabled) iconOn else iconOff
        }
        if (::mouseMenuItem.isInitialized) {
            mouseMenuItem.text = if (panel.interactionEnabled) "Mouse: Off" else "Mouse: On"
        }
    }

    fun newInterfaceButton(): JButton {
        val button = JButton(loadIcon("/icons/new.png"))
        button.toolTipText = "Create a new interface"
        button.addActionListener { addNewInterface() }
        return button
    }

    fun deleteButton(): JButton {
        val button = JButton(loadIcon("/icons/delete_file.png"))
        button.toolTipText = "Remove selected component"
        button.addActionListener { deleteComponent() }
        return button
    }

    fun cleanInterfaceButton(): JButton {
        val button = JButton(loadIcon("/icons/delete.png"))
        button.toolTipText = "Clean the selected interface (remove all components except 0)"
        button.addActionListener { deleteInterface() }
        return button
    }

    fun addSpriteButton(): JButton {
        val button = JButton(loadIcon("/icons/sprite_icon.png"))
        button.toolTipText = "Create a sprite component"
        button.addActionListener { addSprite() }
        return button
    }

    fun addModelButton(): JButton {
        val button = JButton(loadIcon("/icons/model_icon.png"))
        button.toolTipText = "Create a model component"
        button.addActionListener { addModel() }
        return button
    }

    fun addLayerButton(): JButton {
        val button = JButton(loadIcon("/icons/layer_icon.png"))
        button.toolTipText = "Create a layer component"
        button.addActionListener { addLayer() }
        return button
    }

    fun addFigureButton(): JButton {
        val button = JButton(loadIcon("/icons/figure_icon.png"))
        button.toolTipText = "Create a figure component"
        button.addActionListener { addFigure() }
        return button
    }

    fun addTextButton(): JButton {
        val button = JButton(loadIcon("/icons/text_icon.png"))
        button.toolTipText = "Create a text component"
        button.addActionListener { addText() }
        return button
    }

    fun addLineButton(): JButton {
        val button = JButton(loadIcon("/icons/line_icon.png"))
        button.toolTipText = "Create a line component"
        button.addActionListener { addLine() }
        return button
    }

    fun saveButton(): JButton {
        val button = JButton(loadIcon("/icons/save.png"))
        button.toolTipText = "Save the selected component"
        button.addActionListener { saveComponent() }
        return button
    }

    fun multiCopyButton(): JButton
    {
        val icon = ImageIcon(javaClass.getResource("/icons/multi_copy.png"))

        return iconButton(icon, "Copy Components")
        {
            val ui = InterfaceUI.instance ?: return@iconButton
            MultiCopy(ui).isVisible = true
        }
    }

    fun copyButton(): JButton {
        val button = JButton(loadIcon("/icons/copy.png"))
        button.toolTipText = "Copy the selected component"
        button.addActionListener { copyComponent() }
        return button
    }

    fun spriteLibraryButton(): JButton {
        val button = JButton(loadIcon("/icons/sprites_library.png"))
        button.toolTipText = "Allows you to select any sprite loaded from the cache"
        button.addActionListener { openSpriteLibrary() }
        return button
    }

    fun pasteButton(): JButton {
        val button = JButton(loadIcon("/icons/paste.png"))
        button.toolTipText = "Paste the copied component into the selected interface"
        button.addActionListener { pasteComponent() }
        return button
    }

    fun printButton(): JButton {
        val button = JButton(loadIcon("/icons/dump.png"))
        button.toolTipText = "Print either the selected component or the entire interface to a text file"
        button.addActionListener { printComponent() }
        return button
    }

    fun colorPickButton(): JButton {
        val button = JButton("Pick color")
        button.toolTipText = "Select the color"
        button.addActionListener { openColorPicker() }
        return button
    }

    fun fontPreviewButton(): JButton {
        val button = JButton("Font preview")
        button.toolTipText = "Show font examples preview image"

        button.addActionListener {
            val imgStream = InterfaceUIButton::class.java.getResourceAsStream(
                "/misc/interface_font_preview.png"
            )

            if (imgStream == null) {
                JOptionPane.showMessageDialog(
                    null,
                    "Font preview not found in resources, please re-download the application.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val image = ImageIO.read(imgStream)

            val label = JLabel(ImageIcon(image))
            val scroll = JScrollPane(label)

            scroll.preferredSize = Dimension(550, 400)

            JOptionPane.showMessageDialog(
                null,
                scroll,
                "Font Preview",
                JOptionPane.PLAIN_MESSAGE
            )
        }

        return button
    }

    fun toggleMouseButton(): JButton {
        val iconOn = loadIcon("/icons/mouse_on.png")
        mouseButton = JButton(iconOn).apply {
            toolTipText = "Toggle drag & resize"
            addActionListener { toggleMouseAction() }
        }
        return mouseButton
    }
}