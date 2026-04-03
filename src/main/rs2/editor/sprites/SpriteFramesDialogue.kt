package rs2.editor.sprites

import com.alex.loaders.sprites.SpriteArchive
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteFramesDialogue(
    parent: Window?,
    private val spriteId: Int,
    private var archive: SpriteArchive,
    private val onSelect: (frameId: Int) -> Unit,
    private val onArchiveUpdated: ((SpriteArchive) -> Unit)? = null
) : JDialog(parent, "Frames of sprite $spriteId", ModalityType.APPLICATION_MODAL) {
    private var selectedFrame: FrameTile? = null
    private val grid = JPanel(GridLayout(0, 6, 8, 8))

    private val exportBtn = JButton("Export")
    private val addBtn = JButton("Import")
    private val deleteBtn = JButton("Remove")

    init {
        size = Dimension(750, 500)
        setLocationRelativeTo(parent)
        layout = BorderLayout()

        grid.border = EmptyBorder(5, 5, 5, 5)

        val scroll = JScrollPane(grid)
        scroll.verticalScrollBar.unitIncrement = 16
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

        add(scroll, BorderLayout.CENTER)

        exportBtn.isEnabled = false
        deleteBtn.isEnabled = false

        exportBtn.addActionListener { exportSelectedFrame() }
        addBtn.addActionListener { importFrame() }
        deleteBtn.addActionListener { deleteFrame() }

        val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))
        bottom.add(addBtn)
        bottom.add(exportBtn)
        bottom.add(deleteBtn)

        add(bottom, BorderLayout.SOUTH)

        loadFrames()

        isVisible = true
    }

    private fun loadFrames() {
        grid.removeAll()

        for (i in 0 until archive.size()) {
            val img = archive.getSprite(i) ?: continue
            val tile = FrameTile(i, img)
            addFrameMouseListener(tile)
            grid.add(tile)
        }

        selectedFrame = null
        exportBtn.isEnabled = false
        deleteBtn.isEnabled = false

        grid.revalidate()
        grid.repaint()
    }

    private fun addFrameMouseListener(tile: FrameTile) {
        tile.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                selectedFrame?.selected = false
                selectedFrame = tile
                tile.selected = true
                exportBtn.isEnabled = true
                deleteBtn.isEnabled = true
                grid.repaint()

                if (e.clickCount == 2) {
                    onSelect(tile.frameId)
                    dispose()
                }
            }
        })
    }

    private fun importFrame() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return

        val img = ImageIO.read(chooser.selectedFile) ?: return

        if (img.width != archive.width || img.height != archive.height) {
            JOptionPane.showMessageDialog(
                this,
                "Image size must be ${archive.width}x${archive.height}",
                "Invalid Size",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val frames = (0 until archive.size()).mapNotNull { archive.getSprite(it) as? BufferedImage }.toMutableList()
        frames.add(img)

        val newArchive = SpriteArchive(archive.width, archive.height, frames.size)
        frames.forEachIndexed { index, frame -> newArchive.setFrame(index, frame) }

        saveArchive(newArchive)
        archive = newArchive
        onArchiveUpdated?.invoke(newArchive)
        loadFrames()
    }

    private fun deleteFrame() {
        val frame = selectedFrame ?: return
        if (archive.size() <= 1) {
            JOptionPane.showMessageDialog(
                this,
                "Sprite must have at least 1 frame.",
                "Cannot Remove",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val frames = (0 until archive.size())
            .filter { it != frame.frameId }
            .mapNotNull { archive.getSprite(it) as? BufferedImage }
            .toMutableList()

        val newArchive = SpriteArchive(archive.width, archive.height, frames.size)
        frames.forEachIndexed { index, img -> newArchive.setFrame(index, img) }

        saveArchive(newArchive)

        archive = newArchive
        onArchiveUpdated?.invoke(newArchive)

        loadFrames()
    }

    private fun exportSelectedFrame() {
        val frame = selectedFrame ?: return
        val img = archive.getSprite(frame.frameId) ?: return

        val chooser = JFileChooser().apply {
            selectedFile = File("sprite_${spriteId}_frame_${frame.frameId}.png")
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ImageIO.write(img as BufferedImage, "png", chooser.selectedFile)
        }
    }

    private fun saveArchive(newArchive: SpriteArchive) {
        val data = newArchive.encode().array()
        SpriteLoader.put(spriteId, data)
        println("[SpriteCache] Updated sprite $spriteId (${newArchive.size()} frames)")
    }
}