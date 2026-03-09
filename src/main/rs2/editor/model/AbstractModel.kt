package rs2.editor.model

import com.alex.loaders.mesh.Unknown2
import java.awt.event.*
import javax.swing.AbstractListModel

class EditorAbstractListModel2(var1: ModelViewer?, var2: Int, private val field4: Array<String>) :
    AbstractListModel<Any?>() {
    private val field2 = field4

    override fun getSize(): Int {
        return field2.size
    }

    override fun getElementAt(var1: Int): Any? {
        return field2[var1]
    }
}

class EditorAbstractListModel(var1: ModelViewer?, var2: Int, private val field26: Array<String>) :
    AbstractListModel<Any?>() {
    private val field24 = field26

    override fun getSize(): Int {
        return field24.size
    }

    override fun getElementAt(var1: Int): Any? {
        return field24[var1]
    }
}

class EditorComponentAdapter(private val field5: ModelViewer) : ComponentAdapter() {
    override fun componentResized(var1: ComponentEvent) {
        ModelViewer.componentResizedCallback(this.field5, var1)
    }
}
class EditorMouseWheelListener(private val field30: ModelViewer) : MouseWheelListener {
    override fun mouseWheelMoved(var1: MouseWheelEvent) {
        ModelViewer.mouseWheelMoved(this.field30, var1)
    }
}

class EditorMouseAdapter(private val modelFrame: ModelViewer) : MouseAdapter() {
    override fun mousePressed(var1: MouseEvent) {
    }

    override fun mouseReleased(var1: MouseEvent) {
        ModelViewer.mouseReleasedCallback(this.modelFrame, var1)
    }
}

class EditorWindowAdapter(private val field31: ModelViewer) : WindowAdapter() {
    override fun windowClosing(var1: WindowEvent) {
        ModelViewer.windowClosingCallback(this.field31, var1)
    }
}

class ModelRunner : Runnable {
    override fun run() {
        var modelFrame: ModelViewer
        (ModelViewer().also { modelFrame = it }).title = ""
        modelFrame.loadFiles()
        modelFrame.isVisible = true
        modelFrame.render()
        (Thread(modelFrame.toString())).start()
        Unknown2.load()
    }
}
