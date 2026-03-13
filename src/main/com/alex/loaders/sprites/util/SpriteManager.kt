package com.alex.loaders.sprites.util

import com.alex.loaders.sprites.SpriteArchive
import rs2.editor.ui.Consts
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.utils.InterfaceUtils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
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
    private val findBtn = JButton("Find")
    private val previewBtn = JButton("Preview")
    private val helpBtn = JButton(InterfaceUtils.loadIcon("/icons/info.png"))

    val grid = JPanel(WrapLayout(FlowLayout.LEFT, 8, 8))
    private val scroll = JScrollPane(grid)
    private val searchField = JTextField(8)

    private val tileMap = HashMap<Int, SpriteTile>()

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
                val raw = SpriteLoader.getRaw(tile.spriteId)
                val archive = raw?.let { SpriteArchive.decode(it) }
                val width = archive?.width ?: tile.spriteWidth
                val height = archive?.height ?: tile.spriteHeight
                onSelect(tile.spriteId, width, height, null)
                InterfaceUI.instance?.renderPanel?.repaint()
                dispose()
            }
        }

        importBtn.addActionListener { importSprites() }
        exportBtn.addActionListener { exportSelected() }
        exportAllBtn.addActionListener { exportAll() }

        bottom.add(JLabel("Sprite ID:"))
        bottom.add(searchField)
        bottom.add(findBtn)
        bottom.add(previewBtn)
        bottom.add(importBtn)
        bottom.add(exportBtn)
        bottom.add(exportAllBtn)
        bottom.add(selectBtn)
        bottom.add(helpBtn)
        add(bottom, BorderLayout.SOUTH)

        findBtn.addActionListener {
            val id = searchField.text.toIntOrNull() ?: return@addActionListener

            val tile = tileMap[id] ?: return@addActionListener

            selectedTile?.selected = false
            selectedTile = tile
            tile.selected = true
            selectedFrameId = null

            selectBtn.isEnabled = true
            exportBtn.isEnabled = true
            previewBtn.isEnabled = true

            val bounds = tile.bounds
            scroll.viewport.scrollRectToVisible(bounds)
        }
        searchField.apply {
            preferredSize = Dimension(100, Consts.BUTTON_HEIGHT_SMALL)
            minimumSize = Dimension(100, Consts.BUTTON_HEIGHT_SMALL)
            maximumSize = Dimension(100, Consts.BUTTON_HEIGHT_SMALL)
        }
        previewBtn.isEnabled = false

        previewBtn.addActionListener {
            val tile = selectedTile ?: return@addActionListener
            showPreview(tile.spriteId)
        }

        helpBtn.addActionListener { showHelp() }

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
                                previewBtn.isEnabled = true

                                if (e.clickCount == 2) {
                                    val raw = SpriteLoader.getRaw(tile.spriteId)
                                    val currentArchive = raw?.let { SpriteArchive.decode(it) } ?: return@mouseClicked

                                    if (currentArchive.size() > 1) {
                                        SpriteFramesDialogue(
                                            this@SpriteManager,
                                            tile.spriteId,
                                            currentArchive,
                                            onSelect = { frameId ->
                                                onSelect(tile.spriteId, currentArchive.width, currentArchive.height, frameId)
                                            },
                                            onArchiveUpdated = { updatedArchive ->
                                                tile.addFrameIndicator(updatedArchive.size())
                                                grid.revalidate()
                                                grid.repaint()
                                            }
                                        )
                                    } else {
                                        showPreview(tile.spriteId)
                                    }
                                }
                                if (e.clickCount == 2 && (archive == null || archive.size() == 1)) {
                                    showPreview(tile.spriteId)
                                }
                            }
                        },
                    )
                    tileMap[tile.spriteId] = tile
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

    private fun showPreview(spriteId: Int) {
        val img = SpriteLoader.getSprite(spriteId) ?: return

        val dialog = JDialog(this, "Sprite preview ($spriteId)", false)
        dialog.layout = BorderLayout()

        var zoom = 1.0
        val imageLabel = JLabel()

        val previewPanel = JPanel(GridBagLayout()).apply {
            background = Color.DARK_GRAY
            removeAll()
            add(imageLabel)
        }

        fun updateImage() {
            val w = (img.width * zoom).toInt()
            val h = (img.height * zoom).toInt()
            val scaled = img.getScaledInstance(w, h, Image.SCALE_FAST)
            imageLabel.icon = ImageIcon(scaled)
            imageLabel.revalidate()
            imageLabel.repaint()
            previewPanel.revalidate()
            previewPanel.repaint()
        }

        updateImage()

        val scrollPane = JScrollPane(previewPanel)

        scrollPane.addMouseWheelListener {
            zoom *= if (it.preciseWheelRotation < 0) 1.1 else 1 / 1.1
            updateImage()
        }

        val controls = JPanel(FlowLayout(FlowLayout.LEFT))
        val zoomIn = JButton("+")
        val zoomOut = JButton("-")
        val reset = JButton("Reset zoom")
        zoomIn.addActionListener { zoom *= 1.25; updateImage() }
        zoomOut.addActionListener { zoom /= 1.25; updateImage() }
        reset.addActionListener { zoom = 1.0; updateImage() }
        controls.add(JLabel("Zoom:"))
        controls.add(zoomOut)
        controls.add(zoomIn)
        controls.add(reset)

        dialog.add(controls, BorderLayout.NORTH)
        dialog.add(scrollPane, BorderLayout.CENTER)
        dialog.size = Dimension(600, 500)
        dialog.setLocationRelativeTo(this)
        dialog.isVisible = true
    }

    private fun showHelp() {
        val dialog = JDialog(this, "Sprite Library Documentation", false)
        dialog.layout = BorderLayout()

        val helpText = """
            SPRITE LIBRARY – DOCS
            
            Browse Sprites
            --------------
            The grid displays all sprites from the cache.
            Click a sprite to select it.
            
            Select
            ------
            Confirms the selected sprite and returns it to the editor.
            
            Preview
            -------
            Opens the selected sprite in its full original size.
            Preview also supports zooming.
            
            Find
            ----
            Enter a sprite ID and press Find to jump directly to that sprite.
            
            Import
            ------
            Import one or multiple PNG images as a sprite or animation.
            
            Export Selected
            ---------------
            Export the selected sprite (or frames) as PNG files.
            
            Export All
            ----------
            Export every sprite from the cache.
            
            Frames
            ------
            If a sprite contains multiple frames,
            double-click it to open the frame selector.
            
            Tips
            ----
            - Double click a sprite to preview it quickly.
            - Animated sprites open the frame selector.
            - Use Preview to inspect sprite details.
            
            """.trimIndent()

        val textArea = JTextArea(helpText).apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            caretPosition = 0
        }

        val scroll = JScrollPane(textArea)

        dialog.add(scroll, BorderLayout.CENTER)

        dialog.size = Dimension(600, 500)
        dialog.setLocationRelativeTo(this)
        dialog.isVisible = true
    }
}
