package com.editor

import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.JViewport
import javax.swing.Scrollable

/**
 * Holds the panel in which the interface is drawn. The panel stays in the middle while it is smaller than the
 * view, and the view can be scrolled when the panel is larger (large screen modes and zoom).
 */
class CenterPanel : JPanel(GridBagLayout()), Scrollable {

    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 16

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 64

    override fun getScrollableTracksViewportWidth(): Boolean =
        (parent as? JViewport)?.let { it.width > preferredSize.width } ?: true

    override fun getScrollableTracksViewportHeight(): Boolean =
        (parent as? JViewport)?.let { it.height > preferredSize.height } ?: true
}
