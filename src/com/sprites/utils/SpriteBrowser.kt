package com.sprites.utils

import com.sprites.SpriteLoader
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteBrowser(
    parent: JFrame?,
    private val onSelect: (spriteId: Int, width: Int, height: Int) -> Unit
) : JDialog(parent, "Sprite Browser", true) {

    private var selectedTile: SpriteTile? = null

    init {
        size = Dimension(900, 600)
        setLocationRelativeTo(parent)
        layout = BorderLayout()

        val grid = JPanel(WrapLayout(FlowLayout.LEFT, 8, 8))
        grid.background = Color(40, 40, 40)
        grid.border = EmptyBorder(10, 10, 10, 10)

        val scroll = JScrollPane(grid)
        scroll.verticalScrollBar.unitIncrement = 16
        add(scroll, BorderLayout.CENTER)

        val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))
        bottom.background = Color(35, 35, 35)

        val selectBtn = JButton("Choose sprite")
        selectBtn.isEnabled = false

        selectBtn.addActionListener {
            selectedTile?.let {
                onSelect(it.spriteId, it.width, it.height)
                dispose()
            }
        }

        bottom.add(selectBtn)
        add(bottom, BorderLayout.SOUTH)

        object : SwingWorker<Unit, SpriteTile>() {

            override fun doInBackground() {
                for (id in 0 until SpriteLoader.numSprites) {
                    val img = SpriteLoader.getSprite(id) ?: continue
                    publish(SpriteTile(id, img))
                }
            }

            override fun process(chunks: List<SpriteTile>) {
                for (tile in chunks) {
                    tile.addMouseListener(object : java.awt.event.MouseAdapter() {
                        override fun mouseClicked(e: java.awt.event.MouseEvent) {
                            selectedTile?.selected = false
                            selectedTile = tile
                            tile.selected = true
                            selectBtn.isEnabled = true
                        }
                    })
                    grid.add(tile)
                }
                grid.revalidate()
                grid.repaint()
            }
        }.execute()

        isVisible = true
    }
}