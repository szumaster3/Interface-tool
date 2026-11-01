package com.editor

import com.alex.Cache
import com.alex.loaders.items.ItemDefinition
import com.alex.store.Store
import com.editor.properties.PropertyValues
import com.editor.EditorGUIButtons.openSpriteLibrary
import com.editor.utils.ComponentTemplates
import com.editor.utils.Consts
import java.awt.Desktop
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.File
import java.io.IOException
import java.net.URL
import javax.swing.*

object EditorGUIToolbarItems {

    fun newArchiveItem() = JMenuItem("New").apply {
        addActionListener { EditorGUIButtons.addNewInterface() }
    }
    fun openArchiveItem() = JMenuItem("Open").apply {
        isEnabled = false
        addActionListener { EditorGUIButtons.importInterface() }
    }
    fun exportArchiveItem() = JMenuItem("Save as").apply {
        addActionListener { EditorGUIButtons.exportInterface() }
    }
    fun saveArchiveItem() = JMenuItem("Save").apply {
        addActionListener { }
    }

    fun undoItem() = JMenuItem("Undo").apply {
        addActionListener {
            EditorGUI.instance?.compontentRenderPanel?.undo()
            EditorGUI.instance?.compontentRenderPanel?.repaint()
        }
        accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
    }
    fun redoItem() = JMenuItem("Redo").apply {
        addActionListener {
            EditorGUI.instance?.compontentRenderPanel?.redo()
            EditorGUI.instance?.compontentRenderPanel?.repaint()
        }
        accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK)
    }
    fun copyItem() = JMenuItem("Copy").apply {
        addActionListener {
            EditorGUIButtons.copyComponent()
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

    fun addLayerItem() = JMenuItem("Add Layer component").apply {
        addActionListener { EditorGUIButtons.addLayer() }
    }
    fun addFigureItem() = JMenuItem("Add Figure component").apply {
        addActionListener { EditorGUIButtons.addFigure() }
    }
    fun addSpriteItem() = JMenuItem("Add Sprite component").apply {
        addActionListener { EditorGUIButtons.addSprite() }
    }
    fun addModelItem() = JMenuItem("Add Model component").apply {
        addActionListener { EditorGUIButtons.addModel() }
    }
    fun addTextItem() = JMenuItem("Add Text component").apply {
        addActionListener { EditorGUIButtons.addText() }
    }
    fun addLineItem() = JMenuItem("Add Line component").apply {
        addActionListener { EditorGUIButtons.addLine() }
    }

    fun addIf1LayerItem() = JMenuItem("Add Layer component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1Layer() }
    }
    fun addIf1FigureItem() = JMenuItem("Add Figure component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1Figure() }
    }

    fun addIf1SpriteItem() = JMenuItem("Add Sprite component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1Sprite() }
    }

    fun addIf1ModelItem() = JMenuItem("Add Model component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1Model() }
    }

    fun addIf1TextItem() = JMenuItem("Add Text component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1Text() }
    }

    fun addIf1ModelListItem() = JMenuItem("Add Model list component (IF1)").apply {
        addActionListener { EditorGUIButtons.addif1ModelList() }
    }

    fun addIf1InventoryItem() = JMenuItem("Add Inventory component (IF1)").apply {
        addActionListener { EditorGUIButtons.addInventory() }
    }

    fun addIf1TooltipItem() = JMenuItem("Add Tooltip component (IF1)").apply {
        addActionListener { EditorGUIButtons.addToolTip() }
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
        val scrollbarButton = JMenuItem("Layer with scrollbar").apply {
            addActionListener {
                ComponentTemplates.createScrollBarButton()
            }
        }
        buttonsSubMenu.add(simpleButtonItem)
        buttonsSubMenu.add(closeButtonItem)
        buttonsSubMenu.add(tooltipButtonItem)
        buttonsSubMenu.add(spellButtonItem)
        buttonsSubMenu.add(highlightButtonItem)
        buttonsSubMenu.add(dualColorButton)
        buttonsSubMenu.add(scrollbarButton)
        menu.add(buttonsSubMenu)
        return menu
    }

    fun template2Item() = JMenuItem("Default").apply {
        addActionListener { println("Default button clicked") }
        isEnabled = false
    }

    fun toggleMouseMenuItem(): JMenuItem {
        EditorGUIButtons.mouseMenuItem = JMenuItem("Mouse: Off").apply {
            addActionListener { EditorGUIButtons.toggleMouseAction() }
        }
        return EditorGUIButtons.mouseMenuItem
    }

    fun playAnimationItem() = JMenuItem("Play Animation").apply {
        addActionListener { println("Play Animation clicked") }
    }
    fun stopAnimationItem() = JMenuItem("Stop Animation").apply {
        addActionListener { println("Stop Animation clicked") }
    }

    fun viewMediaItem() = JMenuItem("Model Library").apply {
        addActionListener {
            PropertyValues.cache_path?.let { path ->
                try {
                    val store = Store(path)

                    SwingUtilities.invokeLater {
                        val frame = com.editor.util.model.ModelViewerFrame(store)
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

    fun viewSpriteItem() = JMenuItem("Sprite Library").apply {
        addActionListener {
            openSpriteLibrary()
        }
    }

    fun isNewerVersion(remote: String, local: String): Boolean {
        val r = remote.split(".").map { it.toInt() }
        val l = local.split(".").map { it.toInt() }

        val size = maxOf(r.size, l.size)

        for (i in 0 until size) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }

            if (rv > lv) return true
            if (rv < lv) return false
        }

        return false
    }

    fun checkVersion(): JMenuItem = JMenuItem("Check for updates").apply {
        addActionListener {
            Thread {
                try {
                    val localVersion = Consts.APP_VERSION

                    val url = URL("https://raw.githubusercontent.com/szumaster3/Interface-tool/master/version.txt")
                    val remoteVersion = url.readText().trim()

                    if (isNewerVersion(remoteVersion, localVersion)) {

                        SwingUtilities.invokeLater {
                            val result = JOptionPane.showOptionDialog(
                                null,
                                "A new version is available!\n\nCurrent: $localVersion\nLatest: $remoteVersion",
                                "Update Available",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                arrayOf("Go to Source", "Download", "Cancel"),
                                "Download"
                            )

                            when (result) {
                                0 -> Desktop.getDesktop().browse(
                                    URL("https://github.com/szumaster3/Interface-tool/").toURI()
                                )

                                1 -> Desktop.getDesktop().browse(
                                    URL("https://github.com/szumaster3/Interface-tool/releases").toURI()
                                )
                            }
                        }

                    } else {
                        SwingUtilities.invokeLater {
                            JOptionPane.showMessageDialog(
                                null,
                                "You are using the latest version.",
                                "Up to date",
                                JOptionPane.INFORMATION_MESSAGE
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    SwingUtilities.invokeLater {
                        JOptionPane.showMessageDialog(
                            null,
                            "Failed to check for updates.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }.start()
        }
    }

    fun tool1Item() = JMenuItem("Backup cache").apply {
        addActionListener { PropertyValues.backupCache() }
    }

    fun tool2Item() = JMenuItem("Print Item definitions").apply {
        isEnabled = Cache.store != null

        addActionListener {
            val outputFile = "dumps/item_definitions.txt"

            Cache.store?.let { cache ->
                ItemDefinition.print(cache, outputFile)

                JOptionPane.showMessageDialog(
                    this,
                    "Item definitions have been successfully saved!\n\nLocation: ${File(outputFile).absolutePath}",
                    "Export completed",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }
    }

    fun minimizeItem() = JMenuItem("Minimize").apply {
        addActionListener { println("Minimize clicked") }
    }
    fun maximizeItem() = JMenuItem("Maximize").apply {
        addActionListener { println("Maximize clicked") }
    }

    fun documentationItem(): JMenu {
        val menu = JMenu("Documentation")
        val viewSubMenu = JMenu("View Documentation")
        val releaseNotesItem = JMenuItem("Release Notes").apply {
            addActionListener {
                val result = JOptionPane.showConfirmDialog(
                    null,
                    "<html>You are leaving this application and will be<br><center>taken to an external website.<br>Continue?",
                    "Redirected to an external website",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        val url = java.net.URI("https://github.com/szumaster3/Interface-tool/commits/master/")
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(url)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        JOptionPane.showMessageDialog(
                            null,
                            "Failed to open website.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }
        viewSubMenu.add(releaseNotesItem)
        viewSubMenu.addSeparator()

        val apiReferenceItem = JMenuItem("API").apply {
            addActionListener { println("API clicked") }
        }
        val tutorialsItem = JMenuItem("Templates").apply {
            addActionListener { println("Tutorials clicked") }
        }
        viewSubMenu.add(apiReferenceItem)
        viewSubMenu.add(tutorialsItem)
        apiReferenceItem.isEnabled = false
        tutorialsItem.isEnabled = false
        menu.add(viewSubMenu)
        return menu
    }

    fun aboutItem() = JMenuItem("About").apply {
        addActionListener { EditorGUIButtons.aboutDialogue() }
    }
}