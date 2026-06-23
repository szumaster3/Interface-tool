package com.editor

import javax.swing.JMenu
import javax.swing.JMenuBar

class EditorGUIToolbar : JMenuBar() {

    private val fileMenu        = JMenu("File")
    private val editMenu        = JMenu("Edit")
    private val viewMenu        = JMenu("View")
    private val addMenu         = JMenu("Add")
    private val templateMenu    = JMenu("Templates")
    private val controlsMenu    = JMenu("Controls")
    private val animateMenu     = JMenu("Animate")
    private val mediaMenu       = JMenu("Media")
    private val versionControlMenu = JMenu("Version Control")
    private val toolsMenu       = JMenu("Tools")
    private val windowMenu      = JMenu("Window")
    private val helpMenu        = JMenu("Help")

    init {
        // File Menu
        fileMenu.add(EditorGUIToolbarItems.newArchiveItem())
        fileMenu.add(EditorGUIToolbarItems.openArchiveItem())
        fileMenu.add(EditorGUIToolbarItems.exportArchiveItem())
        fileMenu.add(EditorGUIToolbarItems.saveArchiveItem())
        add(fileMenu)

        // Edit Menu
        editMenu.add(EditorGUIToolbarItems.undoItem())
        editMenu.add(EditorGUIToolbarItems.redoItem())
        editMenu.add(EditorGUIToolbarItems.copyItem())
        add(editMenu)

        // View Menu
        viewMenu.add(EditorGUIToolbarItems.zoomInItem())
        viewMenu.add(EditorGUIToolbarItems.zoomOutItem())
        viewMenu.add(EditorGUIToolbarItems.resetViewItem())
        add(viewMenu)
        viewMenu.isEnabled = false

        // Add Menu
        addMenu.add(EditorGUIToolbarItems.addLayerItem())
        addMenu.add(EditorGUIToolbarItems.addFigureItem())
        addMenu.add(EditorGUIToolbarItems.addModelItem())
        addMenu.add(EditorGUIToolbarItems.addSpriteItem())
        addMenu.add(EditorGUIToolbarItems.addTextItem())
        addMenu.add(EditorGUIToolbarItems.addLineItem())
        add(addMenu)

        // Template Menu
        templateMenu.add(EditorGUIToolbarItems.template1Item())
        templateMenu.add(EditorGUIToolbarItems.template2Item())
        add(templateMenu)

        // Controls Menu
        controlsMenu.add(EditorGUIToolbarItems.toggleMouseMenuItem())
        add(controlsMenu)

        // Animate Menu
        animateMenu.add(EditorGUIToolbarItems.playAnimationItem())
        animateMenu.add(EditorGUIToolbarItems.stopAnimationItem())
        add(animateMenu)
        animateMenu.isEnabled = false

        // Media Menu
        mediaMenu.add(EditorGUIToolbarItems.viewMediaItem())
        mediaMenu.add(EditorGUIToolbarItems.viewSpriteItem())
        add(mediaMenu)

        // Version Control Menu
        versionControlMenu.add(EditorGUIToolbarItems.checkVersion())
        add(versionControlMenu)

        // Tools Menu
        toolsMenu.add(EditorGUIToolbarItems.tool1Item())
        toolsMenu.add(EditorGUIToolbarItems.tool2Item())
        add(toolsMenu)

        // Window Menu
        windowMenu.add(EditorGUIToolbarItems.minimizeItem())
        windowMenu.add(EditorGUIToolbarItems.maximizeItem())
        add(windowMenu)
        windowMenu.isEnabled = false

        // Help Menu
        helpMenu.add(EditorGUIToolbarItems.documentationItem())
        helpMenu.add(EditorGUIToolbarItems.aboutItem())
        add(helpMenu)
    }
}
