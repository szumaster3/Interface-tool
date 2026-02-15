package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import rs2.sprites.SpriteLoader
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

class SpriteBrowser(
    parent: JFrame?,
    private val onSelect: (spriteId: Int, width: Int, height: Int, frameId: Int?) -> Unit,
) : JDialog(parent, "Sprite Browser", true) {
    private var selectedTile: SpriteTile? = null
    private var selectedFrameId: Int? = null

    init {
        size = Dimension(990, 600)
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

        val selectBtn = JButton("Choose")
        val importBtn = JButton("Import")
        val exportBtn = JButton("Export selected")
        val exportAllBtn = JButton("Export All")

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

        object : SwingWorker<Unit, SpriteTile>() {
            override fun doInBackground() {
                for (id in 0 until SpriteLoader.numSprites) {
                    val img = SpriteLoader.getSprite(id) ?: continue
                    publish(SpriteTile(id, img))
                }
            }

            override fun process(chunks: List<SpriteTile>) {
                for (tile in chunks) {
                    val raw = SpriteLoader.getRaw(tile.spriteId)
                    val archive = raw?.let { SpriteArchive.decode(it) }

                    if (archive != null && archive.size() > 1) {
                        tile.addFrameIndicator(archive.size())
                    }

                    tile.addMouseListener(
                        object : java.awt.event.MouseAdapter() {
                            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                                selectedTile?.selected = false
                                selectedTile = tile
                                tile.selected = true
                                selectedFrameId = null

                                selectBtn.isEnabled = true
                                exportBtn.isEnabled = true

                                // Podwójne kliknięcie na sprite z wieloma ramkami
                                if (e.clickCount == 2 && archive != null && archive.size() > 1) {
                                    SpriteFramesDialogue(this@SpriteBrowser, tile.spriteId, archive) { frameId ->
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

    private fun importSprites() {
        val chooser =
            JFileChooser().apply {
                isMultiSelectionEnabled = true
                fileFilter = javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png")
            }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return

        val spriteId =
            JOptionPane
                .showInputDialog(this, "Sprite ID:", SpriteLoader.numSprites.toString())
                ?.toIntOrNull() ?: return

        try {
            val archive = SpriteImportUtil.importFromPngs(chooser.selectedFiles.toList())
            SpriteCacheWriter.write(spriteId, archive)
            dispose()
            SpriteBrowser(owner as? JFrame, onSelect)
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

        SpriteExportUtil.exportSprite(archive, tile.spriteId, chooser.selectedFile)
    }

    private fun exportAll() {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return
        val dir = chooser.selectedFile

        for (id in 0 until SpriteLoader.numSprites) {
            val raw = SpriteLoader.getRaw(id) ?: continue
            val archive = SpriteArchive.decode(raw) ?: continue
            SpriteExportUtil.exportSprite(archive, id, File(dir, "sprite_$id"))
        }

        JOptionPane.showMessageDialog(this, "All sprites exported.")
    }
}
