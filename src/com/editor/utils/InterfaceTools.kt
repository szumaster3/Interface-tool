package com.editor.utils

import com.alex.Cache
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.store.Store
import com.editor.EditorGUI
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

/** Actions on whole interfaces: duplicating, removing and copying them to another cache. */
object InterfaceTools {

    private val ui get() = EditorGUI.instance

    private fun index() = Cache.store?.indexes?.getOrNull(Cache.interfaceIndex)

    /** Makes the cache and the lists know about a change of the interfaces and selects [select]. */
    private fun refresh(select: Int?) {
        index()?.resetCachedFiles()
        ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
        ui?.let {
            it.loadInterfaces()
            if (select != null) {
                it.interfacesList.setSelectedValue(select, true)
            }
        }
    }

    /** Copies all the components of the interface to a new interface and selects it. */
    fun duplicate(interfaceId: Int) {
        val index = index() ?: return
        if (!index.archiveExists(interfaceId)) return

        val newId = index.lastArchiveId + 1
        var copied = 0
        for (file in 0..index.getLastFileId(interfaceId)) {
            val data = index.getFile(interfaceId, file) ?: continue
            if (index.putFile(newId, file, data)) copied++
        }
        ComponentNames.copyInterface(interfaceId, newId)
        refresh(newId)
        JOptionPane.showMessageDialog(
            ui,
            "Interface $interfaceId was copied to the new interface $newId ($copied components).\n" +
                    "Scripts which use the id of the interface still use $interfaceId.",
            "Interface duplicated",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    /** Removes the interface from the cache. */
    fun delete(interfaceId: Int) {
        val index = index() ?: return
        val confirm = JOptionPane.showConfirmDialog(
            ui,
            "Are you sure that you want to delete interface $interfaceId?\nThis cannot be undone.",
            "Delete interface",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )
        if (confirm != JOptionPane.YES_OPTION) return

        for (file in index.getLastFileId(interfaceId) downTo 0) {
            if (index.fileExists(interfaceId, file)) index.removeFile(interfaceId, file)
        }
        ComponentNames.removeInterface(interfaceId)
        ui?.clearSelectionAfterRemoval()
        refresh(null)
    }

    /**
     * Copies the components of an interface to the interface [targetId] of another cache, an interface which is
     * there is replaced.
     *
     * @return the number of components which were copied
     */
    fun copy(interfaceId: Int, other: Store, targetId: Int): Int {
        val index = index() ?: return 0
        val target = other.indexes.getOrNull(Cache.interfaceIndex) ?: return 0
        for (file in target.getLastFileId(targetId) downTo 0) {
            if (target.fileExists(targetId, file)) target.removeFile(targetId, file)
        }
        var copied = 0
        for (file in 0..index.getLastFileId(interfaceId)) {
            val data = index.getFile(interfaceId, file) ?: continue
            if (target.putFile(targetId, file, data)) copied++
        }
        return copied
    }

    /** Copies the interface to another cache, by default under the same id. */
    fun copyToCache(interfaceId: Int) {
        val index = index() ?: return
        val chooser = JFileChooser(File(".")).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select the cache the interface is copied to"
        }
        if (chooser.showOpenDialog(ui) != JFileChooser.APPROVE_OPTION) return

        val answer = JOptionPane.showInputDialog(
            ui, "Id of the interface in the other cache (an existing interface is replaced):", interfaceId
        ) ?: return
        val targetId = answer.toString().trim().toIntOrNull()
        if (targetId == null || targetId < 0) {
            JOptionPane.showMessageDialog(ui, "Invalid interface id", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        try {
            val other = Store(chooser.selectedFile.absolutePath + File.separator)
            val target = other.indexes.getOrNull(Cache.interfaceIndex)
            if (target == null) {
                JOptionPane.showMessageDialog(ui, "The cache has no index ${Cache.interfaceIndex}", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }
            if (target.archiveExists(targetId)) {
                val replace = JOptionPane.showConfirmDialog(
                    ui, "Interface $targetId already exists in that cache. Replace it?", "Copy interface",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                )
                if (replace != JOptionPane.YES_OPTION) return
            }
            val copied = copy(interfaceId, other, targetId)
            JOptionPane.showMessageDialog(
                ui, "Copied $copied components of interface $interfaceId to interface $targetId of\n${chooser.selectedFile}",
                "Interface copied", JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(ui, "Could not copy the interface:\n${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }
}
