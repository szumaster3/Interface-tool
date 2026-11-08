package com.editor.utils

import com.alex.Cache
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.editor.EditorGUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

/** Searches the texts (texts, names and options) of the components of all the interfaces. */
object FindInInterfaces {

    private const val MAX_RESULTS = 2000

    class Result(val interfaceId: Int, val componentId: Int, val type: Int, val field: String, val text: String) {
        override fun toString() =
            "$interfaceId:$componentId  (${ComponentType.nameOf(type)})  $field: ${text.replace('\n', ' ')}"
    }

    private var dialog: JDialog? = null

    fun show(parent: Component?) {
        dialog?.let {
            it.toFront()
            it.isVisible = true
            return
        }

        val field = JTextField(24)
        val matchCase = JCheckBox("Match case")
        val search = JButton("Search")
        val status = JLabel(" ")
        val model = DefaultListModel<Result>()
        val results = JList(model)

        fun open(index: Int) {
            val result = model.getElementAt(index) ?: return
            EditorGUI.instance?.openComponent(result.interfaceId, result.componentId)
        }
        results.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) results.locationToIndex(e.point).takeIf { it >= 0 }?.let { open(it) }
            }
        })

        fun run() {
            val query = field.text
            if (query.isEmpty()) return
            model.clear()
            search.isEnabled = false
            status.text = "Searching..."
            object : SwingWorker<List<Result>, Void>() {
                override fun doInBackground() = find(query, matchCase.isSelected)
                override fun done() {
                    val found = try { get() } catch (e: Exception) { emptyList() }
                    found.forEach { model.addElement(it) }
                    status.text = when {
                        found.isEmpty() -> "Nothing found"
                        found.size >= MAX_RESULTS -> "First $MAX_RESULTS results (double click to open)"
                        else -> "${found.size} results (double click to open)"
                    }
                    search.isEnabled = true
                }
            }.execute()
        }
        field.addActionListener { run() }
        search.addActionListener { run() }

        val top = JPanel().apply {
            add(JLabel("Text:"))
            add(field)
            add(matchCase)
            add(search)
        }
        val content = JPanel(BorderLayout(0, 4)).apply {
            border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
            add(top, BorderLayout.NORTH)
            add(JScrollPane(results), BorderLayout.CENTER)
            add(status, BorderLayout.SOUTH)
        }

        dialog = JDialog(SwingUtilities.getWindowAncestor(parent), "Find in interfaces").apply {
            contentPane = content
            size = Dimension(720, 460)
            setLocationRelativeTo(parent)
            addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosed(e: java.awt.event.WindowEvent?) {
                    dialog = null
                }
            })
            defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
            isVisible = true
        }
        field.requestFocusInWindow()
    }

    /** The texts of a component which can be searched, with the name of the field. */
    private fun texts(c: ComponentDefinition): List<Pair<String, String?>> = buildList {
        add("name" to c.name)
        add("text" to c.text)
        add("active text" to c.activeText)
        add("option base" to c.optionBase)
        add("option circumfix" to c.optionCircumfix)
        c.rightClickOptions?.forEachIndexed { i, option -> add("option ${i + 1}" to option) }
        add("button option" to c.option)
        add("option suffix" to c.optionSuffix)
        c.invOptions?.forEachIndexed { i, option -> add("inventory option ${i + 1}" to option) }
    }

    private fun find(query: String, matchCase: Boolean): List<Result> {
        val index = Cache.store?.indexes?.getOrNull(Cache.interfaceIndex) ?: return emptyList()
        val found = ArrayList<Result>()
        for (id in 0..index.lastArchiveId) {
            if (!index.archiveExists(id) || index.getValidFilesCount(id) == 0) continue
            val components = try {
                ComponentDefinition.getInterface(id, false)
            } catch (e: Exception) {
                null
            } ?: continue
            for (component in components) {
                if (component == null) continue
                for ((name, value) in texts(component)) {
                    if (!value.isNullOrEmpty() && value.contains(query, ignoreCase = !matchCase)) {
                        found.add(Result(id, component.componentId, component.type, name, value))
                        if (found.size >= MAX_RESULTS) return found
                        break
                    }
                }
            }
        }
        return found
    }
}
