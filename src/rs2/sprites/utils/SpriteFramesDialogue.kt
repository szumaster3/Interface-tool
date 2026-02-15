package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteFramesDialogue(
    parent: Window?,
    private val spriteId: Int,
    private val archive: SpriteArchive,
    private val onSelect: (frameId: Int) -> Unit,
) : JDialog(parent, "Sprite Frames: $spriteId", ModalityType.APPLICATION_MODAL) {
    private var selectedFrame: FrameTile? = null
    private val grid = JPanel(GridLayout(0, 6, 8, 8))

    init {
        size = Dimension(750, 500)
        setLocationRelativeTo(parent)
        layout = BorderLayout()

        grid.background = Color(45, 45, 45)
        grid.border = EmptyBorder(10, 10, 10, 10)

        val scroll = JScrollPane(grid)
        scroll.verticalScrollBar.unitIncrement = 16
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        add(scroll, BorderLayout.CENTER)

        val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))
        bottom.background = Color(35, 35, 35)

        val chooseBtn = JButton("Choose frame")
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
