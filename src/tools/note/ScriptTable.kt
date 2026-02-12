package tools.note

import properties.PropertyValues
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class ScriptTable : JFrame("Script Table") {
    private val file: File
        get() = File(PropertyValues.scripts_table_path ?: "data/scriptsnotes/scripts_notes.csv")

    private val tableModel = DefaultTableModel(arrayOf("Script ID", "Args", "Description"), 0)
    private val table = JTable(tableModel)
    private val saveBtn = JButton("Save")

    init {
        title = "Script Table"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(850, 500)
        setLocationRelativeTo(null)
        load()
        table.columnModel.getColumn(0).preferredWidth = 80
        table.columnModel.getColumn(0).minWidth = 80
        table.columnModel.getColumn(0).maxWidth = 80

        val scrollPane = JScrollPane(table).apply { preferredSize = Dimension(830, 380) }

        val addRowBtn = JButton("Add Row").apply { addActionListener { tableModel.addRow(arrayOf("", "", "")) } }
        saveBtn.apply { addActionListener { save() } }

        val bottomPanel =
            JPanel().apply {
                add(addRowBtn)
                add(saveBtn)
            }

        add(scrollPane, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        updateButtonState()
        tableModel.addTableModelListener { updateButtonState() }

        table.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val row = table.rowAtPoint(e.point)
                        if (row != -1) {
                            val confirm =
                                JOptionPane.showConfirmDialog(
                                    this@ScriptTable,
                                    "Delete this row?",
                                    "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION,
                                )

                            if (confirm == JOptionPane.YES_OPTION) {
                                tableModel.removeRow(row)
                            }
                        }
                    }
                }
            },
        )

        table.setDefaultRenderer(
            Any::class.java,
            object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable?,
                    value: Any?,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int,
                ): Component {
                    val comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    val isEmptyRow = (0 until tableModel.columnCount).all { tableModel.getValueAt(row, it).toString().isBlank() }
                    if (table != null) {
                        comp.background =
                            if (isEmptyRow) {
                                Color(140, 140, 140)
                            } else if (isSelected) {
                                table.selectionBackground
                            } else {
                                table.background
                            }
                    }
                    return comp
                }
            },
        )

        addWindowListener(
            object : java.awt.event.WindowAdapter() {
                override fun windowClosing(e: java.awt.event.WindowEvent?) {
                    if (saveBtn.isEnabled) save()
                }
            },
        )

        isVisible = true
    }

    private fun load() {
        if (!file.exists()) return
        try {
            file.readLines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("\"Script ID\"")) return@forEach
                val cols = parse(line)
                if (cols.size >= 3 && cols.any { it.isNotBlank() }) {
                    tableModel.addRow(arrayOf(cols[0], cols[1], cols[2]))
                }
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, "Error while loading CSV:\n${e.message}")
        }
    }

    private fun save() {
        try {
            if (table.isEditing) table.cellEditor.stopCellEditing()
            if (tableModel.rowCount == 0 || tableModel.dataVector.all { row -> (row as Vector<*>).all { it.toString().isBlank() } }) return
            file.parentFile?.mkdirs()
            file.printWriter().use { out ->
                out.println("\"Script ID\"|\"Args\"|\"Description\"")
                for (row in 0 until tableModel.rowCount) {
                    val line = (0..2).joinToString("|") { col -> format(tableModel.getValueAt(row, col)) }
                    out.println(line)
                }
            }
            title = "Script Table (Saved)"
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, "Error while saving CSV:\n${e.message}")
        }
    }

    private fun format(value: Any?): String {
        val text = value?.toString() ?: ""
        return "\"" + text.replace("\"", "\"\"") + "\""
    }

    private fun parse(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val char = line[i]
            when (char) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                '|' -> {
                    if (inQuotes) {
                        current.append(char)
                    } else {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                }

                else -> {
                    current.append(char)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    private fun updateButtonState() {
        saveBtn.isEnabled =
            tableModel.rowCount > 0 && tableModel.dataVector.any { row -> (row as Vector<*>).any { it.toString().isNotBlank() } }
    }
}
