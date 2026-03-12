package rs2.editor.ui.util

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.tools.Copy
import java.awt.*
import javax.swing.*
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.utils.InterfaceUtils

class MultiCopy(parent: JFrame) : JDialog(parent, "Copy components", true) {
    private val sourceInterfaceField = JTextField(5)
    private val targetInterfaceField = JTextField(5)
    private val componentsField = JTextField()
    private val startAtField = JTextField("0")
    private val progressBar =
        JProgressBar().apply {
            isIndeterminate = false
            preferredSize = Dimension(195, 25)
        }

    init {
        layout = BorderLayout()
        setSize(290, 190)
        setLocationRelativeTo(parent)

        val form = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }

        fun addRow(row: Int, label: String, field: JTextField, description: String) {
            val labelPanel =
                JPanel(BorderLayout()).apply {
                    border = BorderFactory.createRaisedSoftBevelBorder()
                    preferredSize = Dimension(120, 25)
                    add(JLabel(label), BorderLayout.CENTER)
                }
            val fieldPanel =
                JPanel(BorderLayout()).apply {
                    preferredSize = Dimension(0, 25)
                    add(field, BorderLayout.CENTER)
                    val icon = InterfaceUtils.loadIcon("/icons/info.png")
                    add(
                        JButton(icon).apply {
                            preferredSize = Dimension(25, 25)
                            toolTipText = description
                            addActionListener {
                                JOptionPane.showMessageDialog(
                                    this@MultiCopy,
                                    "<html><body style='width:300px;'>$description</body></html>",
                                    "$label Info",
                                    JOptionPane.INFORMATION_MESSAGE
                                )
                            }
                        },
                        BorderLayout.EAST
                    )
                }

            val rowPanel = JPanel(GridBagLayout())
            val rowGbc =
                GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    gridx = 0
                    weightx = 0.0
                }
            rowPanel.add(labelPanel, rowGbc)
            rowGbc.gridx = 1
            rowGbc.weightx = 1.0
            rowGbc.insets = Insets(1, 3, 0, 0)
            rowPanel.add(fieldPanel, rowGbc)
            gbc.gridy = row
            form.add(rowPanel, gbc)
        }

        addRow(0, "Source Interface", sourceInterfaceField, "Enter Interface ID from which you want to copy components")
        addRow(1, "Target Interface", targetInterfaceField, "Enter the interface ID where components will be copied. Leave empty to create interface as the last interface")
        addRow(2, "Component IDs",    componentsField, "IDs separated by commas 0,1,2 or ranges 0-5")
        addRow(3, "Start At",         startAtField, "Component ID in the target interface from which copying should start (Default set to 0)")

        add(form, BorderLayout.CENTER)

        val buttons = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 3))
        buttons.add(progressBar)
        buttons.add(JButton("Copy").apply { addActionListener { copy() } })
        add(buttons, BorderLayout.SOUTH)
    }

    private fun copy() {
        val srcId = sourceInterfaceField.text.trim().toIntOrNull()
        if (srcId == null) {
            JOptionPane.showMessageDialog(this, "Source Interface must be a valid number", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }

        val startAt = startAtField.text.trim().toIntOrNull() ?: 0

        val targetText = targetInterfaceField.text.trim()
        val targetId =
            if (targetText.isEmpty()) {
                ComponentDefinition.getInterfaceDefinitionsSize()
            } else {
                targetText.toIntOrNull()
                    ?: run {
                        JOptionPane.showMessageDialog(
                            this,
                            "Target Interface must be a valid number",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                        )
                        return
                    }
            }

        val components =
            try {
                parseComponents(componentsField.text.trim())
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(this, "Invalid component IDs", "Warning", JOptionPane.WARNING_MESSAGE)
                return
            }

        if (components.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No components to copy", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }

        isEnabled = false
        progressBar.isIndeterminate = true

        val worker = object : SwingWorker<List<ComponentDefinition>, Unit>() {

            override fun doInBackground(): List<ComponentDefinition> {

                val copier = Copy.to(targetId).from(srcId).startAt(startAt)

                components.forEach { copier.copy(it) }

                val copied = copier.save()

                val targetComponents = ComponentDefinition.getInterface(targetId)
                var nextId = maxOf(startAt, targetComponents?.size ?: 0)

                val idMap = mutableMapOf<Int, Int>()

                copied.forEach { comp ->

                    val oldId = comp.componentId

                    comp.interfaceId = targetId
                    comp.componentId = nextId
                    comp.componentHash = (targetId shl 16) or nextId

                    idMap[oldId] = nextId

                    nextId++
                }

                copied.forEach { comp ->

                    if (comp.parentId != -1) {

                        val oldParent = comp.parentId and 0xFFFF
                        val newParent = idMap[oldParent]

                        if (newParent != null) {
                            comp.parentId = (targetId shl 16) or newParent
                        }
                    }
                }

                return copied
            }

            override fun done() {

                progressBar.isIndeterminate = false
                isEnabled = true

                try {

                    get()

                    ComponentDefinition.getInterface(targetId, true)

                    InterfaceUI.instance?.apply {

                        loadComponents(targetId)

                        if ((0 until interfacesListModel.size()).none { interfacesListModel.getElementAt(it) == targetId }) {
                            interfacesListModel.addElement(targetId)
                        }

                        val index =
                            (0 until interfacesListModel.size()).firstOrNull {
                                interfacesListModel.getElementAt(it) == targetId
                            }

                        index?.let {
                            interfacesList.selectedIndex = it
                            interfacesList.ensureIndexIsVisible(it)
                        }

                        renderPanel?.interfaceId = targetId
                        renderPanel?.repaint()
                    }

                    JOptionPane.showMessageDialog(
                        this@MultiCopy,
                        "Components copied into interface ID=$targetId"
                    )

                } catch (e: Exception) {

                    JOptionPane.showMessageDialog(
                        this@MultiCopy,
                        "Error: ${e.message}"
                    )
                }
            }
        }

        worker.execute()
    }

    private fun parseComponents(text: String): List<Int> {
        if (text.isEmpty()) throw IllegalArgumentException("Component IDs cannot be empty!")

        val list = mutableSetOf<Int>()
        text.split(",").forEach { part ->
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val (startStr, endStr) = trimmed.split("-").map { it.trim() }
                val start = startStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid range start: $startStr")
                val end = endStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid range end: $endStr")
                if (start > end) throw IllegalArgumentException("Range start cannot be greater than end: $start-$end")
                list.addAll(start..end)
            } else {
                list.add(trimmed.toIntOrNull() ?: throw IllegalArgumentException("Invalid component ID: $trimmed"))
            }
        }
        return list.sorted()
    }
}