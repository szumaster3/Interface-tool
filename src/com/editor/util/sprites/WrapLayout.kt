package com.editor.util.sprites

import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout

class WrapLayout(align: Int = LEFT, hgap: Int = 5, vgap: Int = 5) : FlowLayout(align, hgap, vgap)
{
    override fun preferredLayoutSize(target: Container) = layoutSize(target, true)
    override fun minimumLayoutSize(target: Container) = layoutSize(target, false)

    private fun layoutSize(target: Container, preferred: Boolean): Dimension
    {
        synchronized(target.treeLock)
        {
            val insets = target.insets
            val maxWidth = target.width.takeIf { it > 0 } ?: Int.MAX_VALUE

            var width = 0
            var height = insets.top + vgap
            var rowWidth = 0
            var rowHeight = 0

            for (component in target.components)
            {
                if (!component.isVisible) continue

                val d = if (preferred) component.preferredSize else component.minimumSize

                if (rowWidth + d.width > maxWidth)
                {
                    width = maxOf(width, rowWidth)
                    height += rowHeight + vgap
                    rowWidth = 0
                    rowHeight = 0
                }

                rowWidth += d.width + hgap
                rowHeight = maxOf(rowHeight, d.height)
            }

            height += rowHeight + insets.bottom
            width = maxOf(width, rowWidth) + insets.left + insets.right

            return Dimension(width, height)
        }
    }
}
