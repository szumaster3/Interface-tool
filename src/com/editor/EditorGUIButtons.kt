package com.editor

import com.alex.Cache
import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.editor.util.sprites.SpriteManager
import com.alex.util.crc32.CRC32HGenerator
import com.editor.properties.PropertyValues
import com.editor.utils.ColorPicker
import com.editor.utils.ComponentCopy
import com.editor.utils.EditorGenericTextPrinter
import com.editor.utils.FontPreview
import com.editor.utils.InterfaceUtils.iconButton
import com.editor.utils.InterfaceUtils.loadIcon
import com.editor.utils.MultiCopy
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*

object EditorGUIButtons {
    private lateinit var mouseButton: JButton
    lateinit var mouseMenuItem: JMenuItem
    private var copiedComponent: ComponentCopy? = null
    private val hashMap = mutableMapOf<Int, Int>()

    fun addSprite() = EditorGUI.instance?.addComponent(801, 4)
    fun addModel()  = EditorGUI.instance?.addComponent(205, 90) { it.baseX = 0; it.baseY = 0 }
    fun addLayer()  = EditorGUI.instance?.addComponent(778, 0) { it.baseX = 0; it.baseY = 0; it.baseWidth = 50; it.baseHeight = 50 }
    fun addFigure() = EditorGUI.instance?.addComponent(625, 0) { it.baseX = 0; it.baseY = 0 }
    fun addText()   = EditorGUI.instance?.addComponent(752, 5) { it.baseX = 0; it.baseY = 0 }
    fun addLine()   = EditorGUI.instance?.addComponent(658, 39) { it.baseX = 0; it.baseY = 0 }

    fun addif1Sprite() = EditorGUI.instance?.addComponent(63, 8)
    fun addif1Model()  = EditorGUI.instance?.addComponent(74, 2) { it.baseX = 0; it.baseY = 0 }
    fun addif1Layer()  = EditorGUI.instance?.addComponent(63, 10) { it.baseX = 0; it.baseY = 0; it.baseWidth = 50; it.baseHeight = 50 }
    fun addif1Figure() = EditorGUI.instance?.addComponent(76, 8) { it.baseX = 0; it.baseY = 0 }
    fun addif1Text()   = EditorGUI.instance?.addComponent(74, 14) { it.baseX = 0; it.baseY = 0 }

    /** No interface of the cache has a model list, so it is created here. */
    fun addif1ModelList() = EditorGUI.instance?.addComponent(
        ComponentDefinition().apply {
            version = 1
            type = ComponentType.MODEL_LIST
            baseWidth = 100
            baseHeight = 50
            fontId = -1
            unknownProp_11 = -1
        }
    )

    fun addToolTip()    = EditorGUI.instance?.addComponent(78, 32) { it.baseX = 0; it.baseY = 0 }
    fun addInventory()  = EditorGUI.instance?.addComponent(367, 75) { it.baseX = 0; it.baseY = 0 }

    fun addNewInterface() {
        val options = arrayOf("IF1", "IF3")

        val selected = JOptionPane.showOptionDialog(
            EditorGUI.instance,
            "Select interface format:",
            "New Interface",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            "IF3"
        )

        if (selected == -1) return

        addNewInterface(if (selected == 0) 1 else 3)
    }

    private fun addNewInterface(format: Int) {
        val newId = ComponentDefinition.getInterfaceDefinitionsSize()

        val comp = ComponentDefinition().apply {
            componentId = newId
            version = if (format == 1) 1 else 3
            type = ComponentType.SPRITE
            optionBase = ""
            baseX = 0
            baseY = 0
            baseWidth = 18
            baseHeight = 18
            spriteId = 18
            parentId = -1
        }

        val data = when (format) {
            1 -> comp.encodeIf1()
            3 -> comp.encodeIf3()
            else -> return
        }

        Cache.store!!.indexes[3].putFile(newId, 0, data)

        ComponentDefinition.componentDefinition =
            arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())

        EditorGUI.instance?.interfacesListModel?.addElement(newId)
        EditorGUI.instance?.interfacesList?.setSelectedValue(newId, true)
        EditorGUI.instance?.interfacesList?.ensureIndexIsVisible(
            EditorGUI.instance?.interfacesListModel!!.size - 1
        )

        EditorGUI.instance?.compontentRenderPanel?.interfaceId = newId
        EditorGUI.instance?.loadComponents(newId)
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
        val stream = InputStream(data)

        /*
         * Decode everything first, so a broken file never touches the cache.
         */
        val decoded = ArrayList<ComponentDefinition>()
        try {
            while (stream.offset < data.size) {
                val index = decoded.size
                val comp = ComponentDefinition()
                comp.componentHash = index + (interfaceId shl 16)
                comp.decode(stream, index, interfaceId)
                decoded.add(comp)
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                null,
                "Failed to import ${file.name}: the file is not a valid interface (${e.javaClass.simpleName}).",
                "Import failed",
                JOptionPane.ERROR_MESSAGE
            )
            return null
        }
        val components: Array<ComponentDefinition?> = arrayOfNulls(decoded.size)
        for (i in decoded.indices) components[i] = decoded[i]

        val oldCount = try {
            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
        } catch (e: Exception) {
            0
        }

        if (ComponentDefinition.componentDefinition == null) {
            ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        }
        ComponentDefinition.componentDefinition!![interfaceId] = components

        for ((componentId, comp) in components.withIndex()) {
            if (comp != null) {
                val bytes = comp.encode()
                Cache.store!!.indexes[3].putFile(interfaceId, componentId, bytes)
            }
        }
        for (componentId in components.size until oldCount) {
            Cache.store!!.indexes[3].removeFile(interfaceId, componentId)
        }

        EditorGUI.instance?.compontentRenderPanel?.interfaceId = interfaceId
        EditorGUI.instance?.loadComponents(interfaceId)
        EditorGUI.instance?.compontentRenderPanel?.repaint()

        JOptionPane.showMessageDialog(null, "Interface $interfaceId imported from ${file.name}")
        return components
    }

    fun exportInterface()
    {
        val interfaceId = EditorGUI.selectedComponent!!.interfaceId
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
                val bytes = c.encode()
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
        val comp = EditorGUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component first.")
            return
        }

        // component 0 is the base of an IF3 interface, in the old IF1 format it is a normal component
        if (comp.interfaceId < 0 || comp.componentId < 0 || (!comp.isIf1 && comp.componentId == 0)) {
            JOptionPane.showMessageDialog(null, "The base component (0) of an interface cannot be removed.")
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
            val toRemove = LinkedHashSet<ComponentDefinition>()
            fun collect(c: ComponentDefinition) {
                if (!toRemove.add(c)) return
                ComponentDefinition.getChildsByParent(c.interfaceId, c.componentHash).forEach { collect(it) }
            }
            collect(comp)
            for (c in toRemove) {
                Cache.store!!.indexes[3].removeFile(c.interfaceId, c.componentId)
            }
            Cache.store!!.indexes[3].resetCachedFiles()
            Cache.store!!.indexes[3].rewriteTable()

            try {
                Cache.init(PropertyValues.cache_path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } finally {
            ComponentDefinition.getInterface(comp.interfaceId, true)
            EditorGUI.instance?.compontentRenderPanel?.interfaceId = comp.interfaceId
            EditorGUI.instance?.loadComponents(comp.interfaceId)
            EditorGUI.instance?.compontentRenderPanel?.repaint()
        }
    }

    private fun deleteInterface() {
        val interfaceId = EditorGUI.instance?.interfacesList?.selectedValue ?: return
        val confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to clean interface $interfaceId?", "Warning", JOptionPane.YES_NO_OPTION)
        if (confirm != JOptionPane.YES_OPTION) return

        val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
        for (i in 0 until size) {
            if (i == 0) {
                val base = ComponentDefinition.getInterfaceComponent(interfaceId, 0)
                base?.parentId = -1
                Cache.store!!.indexes[3].putFile(interfaceId, 0, base!!.encodeIf3())
            } else {
                Cache.store!!.indexes[3].removeFile(interfaceId, i)
            }
        }
        try {
            Cache.init(PropertyValues.cache_path)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        EditorGUI.instance?.let {
            it.loadComponents(interfaceId)
            it.compontentRenderPanel.let { panel ->
                panel.interfaceId = interfaceId
                panel.repaint()
            }
        }
    }

    private fun saveComponent() {
        val comp = EditorGUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component before saving it.")
            return
        }
        if (comp.interfaceId == -1 || comp.componentId == -1) {
            JOptionPane.showMessageDialog(null, "Please select a valid component & interface before saving it.")
            return
        }
        val store = Cache.store ?: run {
            JOptionPane.showMessageDialog(null, "Cache not loaded.")
            return
        }

        EditorGUI.instance?.save(comp)
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
                comp.encode(),
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
                comp.encode()
            )
        }
        EditorGUI.instance?.loadComponents(comp.interfaceId)
        ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)?.let { EditorGUI.instance?.selectComponent(it) }
        EditorGUI.instance?.compontentRenderPanel?.interfaceId = comp.interfaceId
        EditorGUI.instance?.compontentRenderPanel?.repaint()
    }

    fun buildCopyTree(comp: ComponentDefinition): ComponentCopy {
        val children = EditorInterfaceDraw.childrenCache[comp.componentHash]
            ?.map { buildCopyTree(it) }
            ?: emptyList()

        return ComponentCopy(ComponentDefinition.copy(comp), children)
    }

    fun copyComponent() {
        val comp = EditorGUI.selectedComponent ?: return
        copiedComponent = buildCopyTree(comp)
    }

    private fun pasteComponent() {
        val root = copiedComponent ?: return

        val targetInterfaceId = EditorGUI.instance!!.interfacesList.selectedValue
            ?: return

        hashMap.clear()

        val newRoot = pasteRecursive(root, targetInterfaceId, null)
        ComponentDefinition.getInterface(targetInterfaceId, true)

        EditorGUI.instance?.apply {
            loadComponents(targetInterfaceId)

            ComponentDefinition.getInterfaceComponent(
                targetInterfaceId,
                newRoot.componentId
            )?.let {
                selectComponent(it)
            }

            compontentRenderPanel.interfaceId = targetInterfaceId
            compontentRenderPanel.repaint()
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
        Cache.store!!.indexes[3].putFile(
            targetInterfaceId,
            nextId,
            newComp.encode()
        )
        hashMap[node.component.componentHash] = newComp.componentHash
        node.children.forEach {
            pasteRecursive(it, targetInterfaceId, newComp.componentHash)
        }
        return newComp
    }

    private fun printComponent() {
        val comp = EditorGUI.selectedComponent ?: run {
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
                EditorGenericTextPrinter.printComponent(comp.interfaceId, comp.componentId)
                JOptionPane.showMessageDialog(null, "Component [${comp.componentId}] from interface [${comp.interfaceId}] was exported to data/prints/")
            } else {
                EditorGenericTextPrinter.printInterface(comp.interfaceId)
                val count = ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId)
                JOptionPane.showMessageDialog(null, "Interface [${comp.interfaceId}] with $count components was exported to data/prints/")
            }
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(null, "Error during export: ${ex.message}")
            ex.printStackTrace()
        }
    }

    fun openSpriteLibrary() {
        val comp = EditorGUI.selectedComponent
        if (comp?.interfaceId == -1) {
            JOptionPane.showMessageDialog(null, "Please select a component first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        SpriteManager(null) { spriteId, w, h, _ ->
            comp!!.spriteId = spriteId
            comp.baseWidth = w
            comp.baseHeight = h
            EditorInterfaceDraw.preparedRoots.forEach { root ->
                EditorInterfaceDraw.layoutComponent(root, EditorGUI.instance!!.compontentRenderPanel.width, EditorGUI.instance!!.compontentRenderPanel.height)
            }
            EditorGUI.instance?.compontentRenderPanel?.repaint()
            EditorGUI.instance?.buildPropertyPanel(comp)
        }
    }

    private fun openColorPicker() {
        val comp = EditorGUI.selectedComponent ?: run {
            JOptionPane.showMessageDialog(null, "Please select a component first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        val colorField = EditorGUI.instance?.fieldMap?.get("Color") as? JTextField ?: return
        val picker = ColorPicker(comp, colorField) {
            EditorGUI.instance?.set("Color", comp.color)
            EditorGUI.instance?.repaint()
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

            val authorLabel = JLabel("<html>Made by: Shnek<br>Discord: Cara Shnek#6969<br>Edit by: Szu</html>")
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
        val panel = EditorGUI.instance?.compontentRenderPanel ?: return

        panel.setInteractionEnabled(!panel.interactionEnabled)

        if (EditorGUIButtons::mouseButton.isInitialized) {
            val iconOn = loadIcon("/icons/mouse_on.png")
            val iconOff = loadIcon("/icons/mouse_off.png")
            mouseButton.icon = if (panel.interactionEnabled) iconOn else iconOff
        }
        if (EditorGUIButtons::mouseMenuItem.isInitialized) {
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
            val ui = EditorGUI.instance ?: return@iconButton
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
        button.toolTipText = "Show the fonts of the cache"

        button.addActionListener {
            // the fonts of the cache, drawn like the client does; the image is for caches without fonts
            if (FontPreview.show(EditorGUI.instance)) return@addActionListener

            val imgStream = EditorGUIButtons::class.java.getResourceAsStream(
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