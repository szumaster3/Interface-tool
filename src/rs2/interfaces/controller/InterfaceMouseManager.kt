package rs2.interfaces.controller

import java.util.ArrayDeque

object InterfaceMouseManager {
    private val undoStack = ArrayDeque<() -> Unit>()
    private val redoStack = ArrayDeque<() -> Unit>()

    fun push(
        undo: () -> Unit,
        redo: () -> Unit,
    ) {
        undoStack.addFirst(undo)
        redoStack.clear()
    }

    fun undo() {
        val action = undoStack.pollFirst() ?: return
        action.invoke()
        redoStack.addFirst(action)
    }

    fun redo() {
        val action = redoStack.pollFirst() ?: return
        action.invoke()
        undoStack.addFirst(action)
    }

    fun canUndo() = undoStack.isNotEmpty()

    fun canRedo() = redoStack.isNotEmpty()
}
