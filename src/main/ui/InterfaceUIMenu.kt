package ui

import java.awt.Color
import javax.swing.*

class InterfaceUIMenu : JMenuBar() {

    // File Menu
    private val fileMenu  = JMenu("File")
    private val newFileItem  = JMenuItem("New")
    private val openFileItem = JMenuItem("Open")
    private val saveFileItem = JMenuItem("Save")

    // Edit Menu
    private val editMenu = JMenu("Edit")
    private val undoItem    = JMenuItem("Undo")
    private val redoItem    = JMenuItem("Redo")
    private val copyItem    = JMenuItem("Copy")

    // View Menu
    private val viewMenu        = JMenu("View")
    private val zoomInItem          = JMenuItem("Zoom In")
    private val zoomOutItem         = JMenuItem("Zoom Out")
    private val resetViewItem       = JMenuItem("Reset View")

    // Add Menu
    private val addMenu         = JMenu("Add")
    private val addSpriteItem       = JMenuItem("Add Sprite")
    private val addModelItem        = JMenuItem("Add Model")
    private val addTextItem         = JMenuItem("Add Text")

    // Template Menu
    private val templateMenu    = JMenu("Template")
    private val template1Item       = JMenuItem("Template 1")
    private val template2Item       = JMenuItem("Template 2")

    // Controls Menu
    private val controlsMenu    = JMenu("Controls")
    private val control1Item        = JMenuItem("Control 1")
    private val control2Item        = JMenuItem("Control 2")

    // Animate Menu
    private val animateMenu         = JMenu("Animate")
    private val playAnimationItem       = JMenuItem("Play Animation")
    private val stopAnimationItem       = JMenuItem("Stop Animation")

    // Media Menu
    private val mediaMenu           = JMenu("Media")
    private val importMediaItem         = JMenuItem("Import Media")
    private val exportMediaItem         = JMenuItem("Export Media")

    // Version Control Menu
    private val versionControlMenu  = JMenu("Version Control")
    private val commitItem              = JMenuItem("Commit")
    private val pushItem                = JMenuItem("Push")
    private val pullItem                = JMenuItem("Pull")

    // Tools Menu
    private val toolsMenu = JMenu("Tools")
    private val tool1Item   = JMenuItem("Tool 1")
    private val tool2Item   = JMenuItem("Tool 2")

    // Window Menu
    private val windowMenu  = JMenu("Window")
    private val minimizeItem    = JMenuItem("Minimize")
    private val maximizeItem    = JMenuItem("Maximize")

    // Help Menu
    private val helpMenu            = JMenu("Help")
    private val documentationItem       = JMenuItem("Documentation")
    private val aboutItem               = JMenuItem("About")

    init {

        newFileItem.addActionListener { println("New File clicked") }
        openFileItem.addActionListener { println("Open File clicked") }
        saveFileItem.addActionListener { println("Save File clicked") }
        fileMenu.add(newFileItem)
        fileMenu.add(openFileItem)
        fileMenu.add(saveFileItem)
        add(fileMenu)

        undoItem.addActionListener { println("Undo clicked") }
        redoItem.addActionListener { println("Redo clicked") }
        copyItem.addActionListener { println("Copy clicked") }
        editMenu.add(undoItem)
        editMenu.add(redoItem)
        editMenu.add(copyItem)
        add(editMenu)

        zoomInItem.addActionListener { println("Zoom In clicked") }
        zoomOutItem.addActionListener { println("Zoom Out clicked") }
        resetViewItem.addActionListener { println("Reset View clicked") }
        viewMenu.add(zoomInItem)
        viewMenu.add(zoomOutItem)
        viewMenu.add(resetViewItem)
        add(viewMenu)

        addSpriteItem.addActionListener { println("Add Sprite clicked") }
        addModelItem.addActionListener { println("Add Model clicked") }
        addTextItem.addActionListener { println("Add Text clicked") }
        addMenu.add(addSpriteItem)
        addMenu.add(addModelItem)
        addMenu.add(addTextItem)
        add(addMenu)

        template1Item.addActionListener { println("Template 1 clicked") }
        template2Item.addActionListener { println("Template 2 clicked") }
        templateMenu.add(template1Item)
        templateMenu.add(template2Item)
        add(templateMenu)

        control1Item.addActionListener { println("Control 1 clicked") }
        control2Item.addActionListener { println("Control 2 clicked") }
        controlsMenu.add(control1Item)
        controlsMenu.add(control2Item)
        add(controlsMenu)

        playAnimationItem.addActionListener { println("Play Animation clicked") }
        stopAnimationItem.addActionListener { println("Stop Animation clicked") }
        animateMenu.add(playAnimationItem)
        animateMenu.add(stopAnimationItem)
        add(animateMenu)

        importMediaItem.addActionListener { println("Import Media clicked") }
        exportMediaItem.addActionListener { println("Export Media clicked") }
        mediaMenu.add(importMediaItem)
        mediaMenu.add(exportMediaItem)
        add(mediaMenu)

        commitItem.addActionListener { println("Commit clicked") }
        pushItem.addActionListener { println("Push clicked") }
        pullItem.addActionListener { println("Pull clicked") }
        versionControlMenu.add(commitItem)
        versionControlMenu.add(pushItem)
        versionControlMenu.add(pullItem)
        add(versionControlMenu)

        tool1Item.addActionListener { println("Tool 1 clicked") }
        tool2Item.addActionListener { println("Tool 2 clicked") }
        toolsMenu.add(tool1Item)
        toolsMenu.add(tool2Item)
        add(toolsMenu)

        minimizeItem.addActionListener { println("Minimize clicked") }
        maximizeItem.addActionListener { println("Maximize clicked") }
        windowMenu.add(minimizeItem)
        windowMenu.add(maximizeItem)
        add(windowMenu)

        documentationItem.addActionListener { println("Documentation clicked") }
        aboutItem.addActionListener { println("About clicked") }
        helpMenu.add(documentationItem)
        helpMenu.add(aboutItem)
        add(helpMenu)
    }
}
