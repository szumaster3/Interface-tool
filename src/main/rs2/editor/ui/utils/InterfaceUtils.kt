package rs2.editor.ui.utils

import com.alex.loaders.interfaces.ComponentDefinition
import rs2.editor.ui.Consts.fillColors
import rs2.editor.ui.Consts.outlineColors
import java.awt.*
import javax.swing.*

object InterfaceUtils {

    @JvmStatic
    fun drawOutlines(g2: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, selected: Boolean)
    {
        if (!selected) return

        val fillColor = fillColors[comp.type] ?: Color.GRAY
        g2.color = fillColor
        g2.fillRect(x, y, w, h)

        val outlineColor = outlineColors[comp.type] ?: Color.DARK_GRAY
        g2.color = outlineColor
        g2.stroke = BasicStroke(1f)
        g2.drawRect(x, y, w, h)
    }

    @JvmStatic
    fun updateRightClickOption(c: ComponentDefinition, index: Int, value: String) {
        if (c.rightClickOptions == null) {
            c.rightClickOptions = arrayOfNulls(10)
        }
        if (index in c.rightClickOptions!!.indices) {
            c.rightClickOptions!![index] = value
        }
    }

    @JvmStatic
    fun createSpinner(max: Int? = Int.MAX_VALUE, step: Int = 1, font: Font? = null): JSpinner
    {
        val spinner = JSpinner(SpinnerNumberModel(0, Int.MIN_VALUE, max, step))
        val editor  = JSpinner.NumberEditor(spinner, "#,##0")
        val dfs     = editor.format.decimalFormatSymbols

        dfs.groupingSeparator = ','
        dfs.decimalSeparator  = '.'
        editor.format.decimalFormatSymbols = dfs
        editor.format.isGroupingUsed = true

        spinner.editor = editor
        font?.let {
            spinner.font = it
            (editor.textField as JFormattedTextField).font = it
        }
        return spinner
    }

    @JvmStatic
    fun loadIcon(path: String): ImageIcon {
        val url = javaClass.getResource(path)
        return if (url != null) ImageIcon(url) else ImageIcon()
    }

    fun iconButton(
        icon: Icon,
        tooltip: String,
        onClick: () -> Unit
    ): JButton {
        return JButton(icon).apply {
            toolTipText = tooltip
            isFocusPainted = false
            isContentAreaFilled = false
            isBorderPainted = true
            margin = Insets(0, 0, 0, 0)
            preferredSize = Dimension(18, 18)
            addActionListener { onClick() }
        }
    }
}