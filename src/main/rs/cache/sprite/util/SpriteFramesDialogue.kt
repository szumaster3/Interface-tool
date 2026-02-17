package rs.cache.sprite.util

import rs.cache.sprite.SpriteArchive
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteFramesDialogue(parent: Window?, private val spriteId: Int, private val archive: SpriteArchive, private val onSelect: (frameId: Int) -> Unit) : JDialog(parent, "You are viewing the frames of sprite id: $spriteId", ModalityType.APPLICATION_MODAL) {

    private var selectedFrame: FrameTile? = null
    private val grid = JPanel(GridLayout(0, 6, 8, 8))

    init {
        size = Dimension(750, 500)
        setLocationRelativeTo(parent)
        layout = BorderLayout()

        grid.border = EmptyBorder(3, 3, 3, 3)

        val scroll = JScrollPane(grid)
        scroll.verticalScrollBar.unitIncrement = 16
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        add(scroll, BorderLayout.CENTER)

        val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))

        val chooseBtn = JButton("Select")
        chooseBtn.isEnabled = false

        chooseBtn.addActionListener {
            selectedFrame?.let {
                onSelect(it.frameId)
                dispose()
            }
        }

        bottom.add(chooseBtn)
        add(bottom, BorderLayout.SOUTH)

        loadFrames(chooseBtn)

        isVisible = true
    }

    private fun loadFrames(chooseBtn: JButton) {
        for (i in 0 until archive.size()) {
            val img = archive.getSprite(i) ?: continue
            val tile = FrameTile(i, img)

            tile.addMouseListener(
                object : java.awt.event.MouseAdapter() {
                    override fun mouseClicked(e: java.awt.event.MouseEvent) {
                        selectedFrame?.selected = false
                        selectedFrame = tile
                        tile.selected = true
                        chooseBtn.isEnabled = true
                        if (e.clickCount == 2) {
                            onSelect(tile.frameId)
                            dispose()
                        }
                    }
                },
            )

            grid.add(tile)
        }

        grid.revalidate()
        grid.repaint()
    }
}
