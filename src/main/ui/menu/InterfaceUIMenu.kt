package ui.menu

import javax.swing.*

class InterfaceUIMenu : JMenuBar() {

    private val fileMenu        = JMenu("File")
    private val editMenu        = JMenu("Edit")
    private val viewMenu        = JMenu("View")
    private val addMenu         = JMenu("Add")
    private val templateMenu    = JMenu("Template")
    private val controlsMenu    = JMenu("Controls")
    private val animateMenu     = JMenu("Animate")
    private val mediaMenu       = JMenu("Media")
    private val versionControlMenu = JMenu("Version Control")
    private val toolsMenu       = JMenu("Tools")
    private val windowMenu      = JMenu("Window")
    private val helpMenu        = JMenu("Help")

    init {
        // File Menu
        fileMenu.add(InterfaceUIMenuItems.newArchiveItem())
        fileMenu.add(InterfaceUIMenuItems.cleanArchiveItem())
        fileMenu.add(InterfaceUIMenuItems.openArchiveItem())
        fileMenu.add(InterfaceUIMenuItems.saveArchiveItem())
        add(fileMenu)

        // Edit Menu
        editMenu.add(InterfaceUIMenuItems.undoItem())
        editMenu.add(InterfaceUIMenuItems.redoItem())
        editMenu.add(InterfaceUIMenuItems.copyItem())
        add(editMenu)
        editMenu.isEnabled = false

        // View Menu
        viewMenu.add(InterfaceUIMenuItems.zoomInItem())
        viewMenu.add(InterfaceUIMenuItems.zoomOutItem())
        viewMenu.add(InterfaceUIMenuItems.resetViewItem())
        add(viewMenu)
        viewMenu.isEnabled = false

        // Add Menu
        addMenu.add(InterfaceUIMenuItems.addLayerItem())
        addMenu.add(InterfaceUIMenuItems.addFigureItem())
        addMenu.add(InterfaceUIMenuItems.addModelItem())
        addMenu.add(InterfaceUIMenuItems.addSpriteItem())
        addMenu.add(InterfaceUIMenuItems.addTextItem())
        addMenu.add(InterfaceUIMenuItems.addLineItem())
        add(addMenu)

        // Template Menu
        templateMenu.add(InterfaceUIMenuItems.template1Item())
        templateMenu.add(InterfaceUIMenuItems.template2Item())
        add(templateMenu)
        templateMenu.isEnabled = false

        // Controls Menu
        controlsMenu.add(InterfaceUIMenuItems.control1Item())
        controlsMenu.add(InterfaceUIMenuItems.control2Item())
        add(controlsMenu)
        controlsMenu.isEnabled = false

        // Animate Menu
        animateMenu.add(InterfaceUIMenuItems.playAnimationItem())
        animateMenu.add(InterfaceUIMenuItems.stopAnimationItem())
        add(animateMenu)
        animateMenu.isEnabled = false

        // Media Menu
        mediaMenu.add(InterfaceUIMenuItems.importMediaItem())
        mediaMenu.add(InterfaceUIMenuItems.exportMediaItem())
        add(mediaMenu)
        mediaMenu.isEnabled = false

        // Version Control Menu
        versionControlMenu.add(InterfaceUIMenuItems.commitItem())
        versionControlMenu.add(InterfaceUIMenuItems.pushItem())
        versionControlMenu.add(InterfaceUIMenuItems.pullItem())
        add(versionControlMenu)
        versionControlMenu.isEnabled = false

        // Tools Menu
        toolsMenu.add(InterfaceUIMenuItems.tool1Item())
        toolsMenu.add(InterfaceUIMenuItems.tool2Item())
        add(toolsMenu)
        toolsMenu.isEnabled = false

        // Window Menu
        windowMenu.add(InterfaceUIMenuItems.minimizeItem())
        windowMenu.add(InterfaceUIMenuItems.maximizeItem())
        add(windowMenu)
        windowMenu.isEnabled = false

        // Help Menu
        helpMenu.add(InterfaceUIMenuItems.documentationItem())
        helpMenu.add(InterfaceUIMenuItems.aboutItem())
        add(helpMenu)
    }
}
