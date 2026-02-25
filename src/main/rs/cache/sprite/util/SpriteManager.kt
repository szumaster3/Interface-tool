package rs.cache.sprite.util

import rs.cache.sprite.SpriteArchive
import rs.cache.sprite.SpriteLoader
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteManager(parent: JFrame?, private val onSelect: (spriteId: Int, width: Int, height: Int, frameId: Int?) -> Unit) : JDialog(parent, "Sprite Library", true) {
    private var selectedTile: SpriteTile? = null
    private var selectedFrameId: Int? = null
    private val bottom = JPanel(FlowLayout(FlowLayout.RIGHT))

    val selectBtn = JButton("Select")
    val exportBtn = JButton("Export selected")
    private val importBtn = JButton("Import")
    private val exportAllBtn = JButton("Export all")
    val grid = JPanel(WrapLayout(FlowLayout.LEFT, 8, 8))
    private val scroll = JScrollPane(grid)

    init
    {
        size = Dimension(1024, 512)
        setLocationRelativeTo(parent)
        layout = BorderLayout()
        grid.border = EmptyBorder(5, 5, 5, 5)
        scroll.verticalScrollBar.unitIncrement = 16
        add(scroll, BorderLayout.CENTER)
        selectBtn.isEnabled = false
        exportBtn.isEnabled = false
        selectBtn.addActionListener {
            selectedTile?.let { tile ->
                onSelect(tile.spriteId, tile.spriteWidth, tile.spriteHeight, selectedFrameId)
                dispose()
            }
        }

        importBtn.addActionListener { importSprites() }
        exportBtn.addActionListener { exportSelected() }
        exportAllBtn.addActionListener { exportAll() }

        bottom.add(importBtn)
        bottom.add(exportBtn)
        bottom.add(exportAllBtn)
        bottom.add(selectBtn)
        add(bottom, BorderLayout.SOUTH)

        object : SwingWorker<Unit, SpriteTile>()
        {
            override fun doInBackground() {
                for (id in 0 until SpriteLoader.numSprites)
                {
                    val img = SpriteLoader.getSprite(id) ?: continue
                    publish(SpriteTile(id, img))
                }
            }

            override fun process(chunks: List<SpriteTile>)
            {
                for (tile in chunks) {
                    val raw = SpriteLoader.getRaw(tile.spriteId)
                    val archive = raw?.let { SpriteArchive.decode(it) }

                    if (archive != null && archive.size() > 1) {
                        tile.addFrameIndicator(archive.size())
                    }

                    tile.addMouseListener(
                        object : java.awt.event.MouseAdapter()
                        {
                            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                                selectedTile?.selected = false
                                selectedTile = tile
                                tile.selected = true
                                selectedFrameId = null

                                selectBtn.isEnabled = true
                                exportBtn.isEnabled = true

                                if (e.clickCount == 2 && archive != null && archive.size() > 1) {
                                    SpriteFramesDialogue(this@SpriteManager, tile.spriteId, archive) { frameId ->
                                        selectedFrameId = frameId
                                        onSelect(tile.spriteId, archive.width, archive.height, frameId)
                                        dispose()
                                    }
                                }
                            }
                        },
                    )

                    grid.add(tile)
                }
                grid.revalidate()
                grid.repaint()
            }
        }.execute()

        isVisible = true
    }

    private fun importSprites()
    {
        val chooser = JFileChooser().apply {
            isMultiSelectionEnabled = true
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png")
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return

        val spriteId =
            JOptionPane.showInputDialog(this, "SpriteId:", SpriteLoader.numSprites.toString())?.toIntOrNull() ?: return

        try {
            val archive = import(chooser.selectedFiles.toList())
            write(spriteId, archive)
            dispose()
            SpriteManager(owner as? JFrame, onSelect)
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, e.message, "Import Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun exportSelected() {
        val tile = selectedTile ?: return
        val raw = SpriteLoader.getRaw(tile.spriteId) ?: return
        val archive = SpriteArchive.decode(raw) ?: return

        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return
        exportSprite(archive, tile.spriteId, chooser.selectedFile)
    }

    private fun exportAll() {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return
        val dir = chooser.selectedFile

        for (id in 0 until SpriteLoader.numSprites) {
            val raw = SpriteLoader.getRaw(id) ?: continue
            val archive = SpriteArchive.decode(raw) ?: continue
            exportSprite(archive, id, File(dir, "sprite_$id"))
        }

        JOptionPane.showMessageDialog(this, "All sprites exported.")
    }

    private fun exportSprite(archive: SpriteArchive, spriteId: Int, directory: File) {
        directory.mkdirs()

        for (i in 0 until archive.size()) {
            val img = archive.getSprite(i) ?: continue
            val file = File(directory, "${spriteId}_$i.png")
            ImageIO.write(img, "png", file)
        }

        println("[SpriteExport] Exported sprite $spriteId to ${directory.absolutePath}")
    }

    fun import(files: List<File>): SpriteArchive {
        require(files.isNotEmpty()) { "No PNG files selected." }

        val images = files.map {
            ImageIO.read(it) ?: error("Failed to read PNG: ${it.name}")
        }

        val width = images.first().width
        val height = images.first().height

        images.forEach {
            require(it.width == width && it.height == height) {
                "All PNG files must have the same dimensions."
            }
        }

        val archive = SpriteArchive(width, height, images.size)
        images.forEachIndexed { index, image ->
            archive.setFrame(index, image)
        }

        println("[SpriteImport] Imported ${images.size} frame(s) (${width}x$height)")
        return archive
    }

    private fun write(spriteId: Int, archive: SpriteArchive) {
        val data = archive.encode().array()
        SpriteLoader.put(spriteId, data)
        println("[SpriteCache] Sprite $spriteId written to cache (${data.size} bytes)")
    }
}
