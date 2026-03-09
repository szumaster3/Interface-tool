package rs2.editor.ui.menu

import com.alex.store.Index
import com.alex.store.Store
import rs2.editor.model.ModelViewerFrame
import rs2.editor.model.MultiModelPacker
import rs2.editor.properties.PropertyValues
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.utils.ComponentTemplates
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JOptionPane

object InterfaceUIMenuItems {

    fun newArchiveItem() = JMenuItem("New").apply {
        addActionListener { InterfaceUIButton.addNewInterface() }
    }
    fun openArchiveItem() = JMenuItem("Open").apply {
        addActionListener { InterfaceUIButton.importInterface() }
    }
    fun exportArchiveItem() = JMenuItem("Save as").apply {
        addActionListener { InterfaceUIButton.exportInterface() }
    }
    fun saveArchiveItem() = JMenuItem("Save").apply {
        addActionListener { }
    }

    fun undoItem() = JMenuItem("Undo").apply {
        addActionListener {
            InterfaceUI.instance?.renderPanel?.undo()
            InterfaceUI.instance?.renderPanel?.repaint()
        }
    }
    fun redoItem() = JMenuItem("Redo").apply {
        addActionListener {
            InterfaceUI.instance?.renderPanel?.redo()
            InterfaceUI.instance?.renderPanel?.repaint()
        }
    }
    fun copyItem() = JMenuItem("Copy").apply {
        addActionListener {
            InterfaceUIButton.copyComponent()
        }
    }

    fun zoomInItem() = JMenuItem("Zoom In").apply {
        addActionListener { }
    }
    fun zoomOutItem() = JMenuItem("Zoom Out").apply {
        addActionListener { }
    }
    fun resetViewItem() = JMenuItem("Reset View").apply {
        addActionListener { }
    }

    fun addLayerItem() = JMenuItem("Add Layer").apply {
        addActionListener { InterfaceUIButton.addLayer() }
    }
    fun addFigureItem() = JMenuItem("Add Figure").apply {
        addActionListener { InterfaceUIButton.addFigure() }
    }
    fun addSpriteItem() = JMenuItem("Add Sprite").apply {
        addActionListener { InterfaceUIButton.addSprite() }
    }
    fun addModelItem() = JMenuItem("Add Model").apply {
        addActionListener { InterfaceUIButton.addModel() }
    }
    fun addTextItem() = JMenuItem("Add Text").apply {
        addActionListener { InterfaceUIButton.addText() }
    }
    fun addLineItem() = JMenuItem("Add Line").apply {
        addActionListener { InterfaceUIButton.addLine() }
    }

    fun template1Item(): JMenu {
        val menu = JMenu("Scripts")

        val buttonsSubMenu = JMenu("Buttons")

        val simpleButtonItem = JMenuItem("Simple button").apply {
            addActionListener {
                ComponentTemplates.createSimpleButton()
            }
        }
        val tooltipButtonItem = JMenuItem("Tooltip button").apply {
            addActionListener {
                ComponentTemplates.createTooltipButton()
            }
        }
        val spellButtonItem = JMenuItem("Spell Button").apply {
            addActionListener {
                ComponentTemplates.createSpellButton()
            }
        }
        val highlightButtonItem = JMenuItem("Highlight Button").apply {
            addActionListener {
                ComponentTemplates.createHighlightButton()
            }
        }
        val closeButtonItem = JMenuItem("Close Button").apply {
            addActionListener {
                ComponentTemplates.createCloseButton()
            }
        }
        val dualColorButton = JMenuItem("Simple dual color Button").apply {
            addActionListener {
                ComponentTemplates.createDualColorButton()
            }
        }

        buttonsSubMenu.add(simpleButtonItem)
        buttonsSubMenu.add(closeButtonItem)
        buttonsSubMenu.add(tooltipButtonItem)
        buttonsSubMenu.add(spellButtonItem)
        buttonsSubMenu.add(highlightButtonItem)
        buttonsSubMenu.add(dualColorButton)
        menu.add(buttonsSubMenu)
        return menu
    }

    fun template2Item() = JMenuItem("Default").apply {
        addActionListener { println("Default button clicked") }
        isEnabled = false
    }

    fun control1Item() = JMenuItem("Control 1").apply {
        addActionListener { println("Control 1 clicked") }
    }
    fun control2Item() = JMenuItem("Control 2").apply {
        addActionListener { println("Control 2 clicked") }
    }

    fun playAnimationItem() = JMenuItem("Play Animation").apply {
        addActionListener { println("Play Animation clicked") }
    }
    fun stopAnimationItem() = JMenuItem("Stop Animation").apply {
        addActionListener { println("Stop Animation clicked") }
    }

    fun viewMediaItem() = JMenuItem("View media").apply {
        addActionListener {
            PropertyValues.cache_path?.let { path ->
                try {
                    val store = Store(path)

                    javax.swing.SwingUtilities.invokeLater {
                        val frame = ModelViewerFrame(store)
                        frame.isVisible = true
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    JOptionPane.showMessageDialog(
                        null,
                        "Failed to load cache: ${ex.message}",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }
    }

    fun importMediaItem() = JMenuItem("Import media").apply {
        addActionListener {
            val packer = MultiModelPacker()
            PropertyValues.cache_path?.let { it1 -> packer.showWindow(it1) }
        }
    }

    fun exportMediaItem() = JMenuItem("Export media").apply {
        addActionListener {
            val cacheChooser = JFileChooser()
            cacheChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            cacheChooser.dialogTitle = "Select Cache Directory"
            val cacheResult = cacheChooser.showOpenDialog(null)
            if (cacheResult != JFileChooser.APPROVE_OPTION) return@addActionListener
            val cacheDir = cacheChooser.selectedFile.absolutePath

            val outputChooser = JFileChooser()
            outputChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            outputChooser.dialogTitle = "Select Output Directory"
            val outputResult = outputChooser.showSaveDialog(null)
            if (outputResult != JFileChooser.APPROVE_OPTION) return@addActionListener
            val outputDir = outputChooser.selectedFile

            try {
                val store = Store(cacheDir)
                val index: Index = store.indexes[7]
                val lastArchive = index.lastArchiveId

                for (i in 0 until lastArchive) {
                    val data: ByteArray? = index.getFile(i)
                    if (data != null) {
                        writeFile(data, File(outputDir, "$i.dat").absolutePath)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    @Throws(IOException::class)
    fun writeFile(data: ByteArray, fileName: String) {
        FileOutputStream(fileName).use { it.write(data) }
    }

    fun commitItem() = JMenuItem("Commit").apply {
        addActionListener { println("Commit clicked") }
    }
    fun pushItem() = JMenuItem("Push").apply {
        addActionListener { println("Push clicked") }
    }
    fun pullItem() = JMenuItem("Pull").apply {
        addActionListener { println("Pull clicked") }
    }

    fun tool1Item() = JMenuItem("Tool 1").apply {
        addActionListener { println("Tool 1 clicked") }
    }
    fun tool2Item() = JMenuItem("Tool 2").apply {
        addActionListener { println("Tool 2 clicked") }
    }

    fun minimizeItem() = JMenuItem("Minimize").apply {
        addActionListener { println("Minimize clicked") }
    }
    fun maximizeItem() = JMenuItem("Maximize").apply {
        addActionListener { println("Maximize clicked") }
    }

    fun documentationItem() = JMenuItem("Documentation").apply {
        addActionListener { println("Documentation clicked") }
    }
    fun aboutItem() = JMenuItem("About").apply {
        addActionListener { InterfaceUIButton.aboutDialogue() }
    }
}