package rs.cache.sprite.util

import rs.cache.sprite.SpriteArchive
import rs.cache.sprite.SpriteLoader
import java.awt.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteFramesDialogue(
    parent: Window?,
    private val spriteId: Int,
    private val archive: SpriteArchive,
    private val onSelect: (frameId: Int) -> Unit
) : JDialog(parent, "Frames of sprite $spriteId", ModalityType.APPLICATION_MODAL) {

    private var selectedFrame: FrameTile? = null
    private val grid = JPanel(GridLayout(0, 6, 8, 8))

    private val chooseBtn = JButton("Select")
    private val exportBtn = JButton("Export")
    private val addBtn = JButton("Import")
    private val deleteBtn = JButton("Remove")

    init {
        size = Dimension(750, 500)
        setLocationRelativeTo(parent)
        layout = BorderLayout()

        grid.border = EmptyBorder(3, 3, 3, 3)
        val scroll = JScrollPane(grid)
        scroll.verticalScrollBar.unitIncrement = 16
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        add(scroll, BorderLayout.CENTER)

        chooseBtn.isEnabled = false
        exportBtn.isEnabled = false
        deleteBtn.isEnabled = false

        chooseBtn.addActionListener {
            selectedFrame?.let { onSelect(it.frameId) }
            dispose()
        }

        exportBtn.addActionListener {
            selectedFrame?.let { frame ->
                val img = archive.getSprite(frame.frameId) ?: return@let
                val chooser = JFileChooser().apply {
                    selectedFile = File("frame_${frame.frameId}.png")
                }
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    ImageIO.write(img as? java.awt.image.BufferedImage, "png", chooser.selectedFile)
                }
            }
        }

        addBtn.addActionListener {
            val chooser = JFileChooser()
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val img = ImageIO.read(chooser.selectedFile)
                val newIndex = archive.size()
                archive.setFrame(newIndex, img)
                val tile = FrameTile(newIndex, img)
                addFrameMouseListener(tile)
                grid.add(tile)
                grid.revalidate()
                grid.repaint()
            }
        }

        deleteBtn.addActionListener {
            selectedFrame?.let { frame ->
                SpriteLoader.removeArchive(frame.frameId)
                grid.remove(frame)
                selectedFrame = null
                chooseBtn.isEnabled = false
                exportBtn.isEnabled = false
                deleteBtn.isEnabled = false
                grid.revalidate()
                grid.repaint()
            }
        }

        val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))
        bottom.add(addBtn)
        bottom.add(exportBtn)
        bottom.add(deleteBtn)
        bottom.add(chooseBtn)
        add(bottom, BorderLayout.SOUTH)

        loadFrames()

        isVisible = true
    }

    private fun loadFrames() {
        for (i in 0 until archive.size()) {
            val img = archive.getSprite(i) ?: continue
            val tile = FrameTile(i, img)
            addFrameMouseListener(tile)
            grid.add(tile)
        }
        grid.revalidate()
        grid.repaint()
    }

    private fun addFrameMouseListener(tile: FrameTile) {
        tile.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                selectedFrame?.selected = false
                selectedFrame = tile
                tile.selected = true

                chooseBtn.isEnabled = true
                exportBtn.isEnabled = true
                deleteBtn.isEnabled = true

                if (e.clickCount == 2) {
                    onSelect(tile.frameId)
                    dispose()
                }
            }
        })
    }
}