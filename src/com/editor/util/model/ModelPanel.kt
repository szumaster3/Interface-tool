package com.editor.util.model

import com.alex.loaders.models.Model
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.*
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import javax.swing.GroupLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class ModelPanel : JPanel(), Runnable {
    var loaded: Boolean = false
    private val oriPosX = 270
    private val oriPosY = 280
    private var posX = oriPosX
    private var posY = oriPosY
    private var axisX = 0
    private var axisY = 0
    private var axisZ = 500
    private var screenX = -1
    private var screenY = -1
    @Volatile
    private var model: Model? = null
    private var renderer: ModelViewerRenderer? = null

    init {
        background = Color(0, 0, 0)

        addMouseWheelListener { e -> mouseWheelMovedCallback(e) }

        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                mouseReleasedCallback(e)
            }
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(c: ComponentEvent) {
                componentResizedCallback(c)
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                mouseDraggedCallback(e)
            }
        })

        val infoLabel = JLabel("Choose a model ID from the list on the left")
        infoLabel.foreground = Color(180, 180, 180)
        infoLabel.font = Font("Arial", Font.PLAIN, 16)
        infoLabel.horizontalAlignment = SwingConstants.CENTER
        infoLabel.verticalAlignment = SwingConstants.CENTER

        val layout = GroupLayout(this)
        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        setLayout(layout)

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 536, Int.MAX_VALUE)
                .addGap(0, 536, Int.MAX_VALUE)
        )

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 318, Int.MAX_VALUE)
                .addGap(0, 318, Int.MAX_VALUE)
        )
    }

    override fun run() {
        while (true) {

            val g: Graphics? = graphics

            val currentModel = model
            val currentRenderer = renderer
            if (currentModel != null && currentRenderer != null && g != null) {
                try {
                    currentRenderer.render(currentModel, posX, posY, axisY, axisX, axisZ)
                    currentRenderer.draw(g)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                Thread.sleep(10L)
            } catch (_: InterruptedException) {}

            if (System.currentTimeMillis() - gcDelay > 60000L) {
                System.gc()
                gcDelay = System.currentTimeMillis()
            }
        }
    }

    private fun getBytesFromFile(fileName: String): ByteArray? {
        try {
            var length: Int
            val bytes = ByteArray((File(fileName)).length().toInt().also { length = it })
            var `in`: DataInputStream
            (DataInputStream(BufferedInputStream(FileInputStream(fileName))).also { `in` = it })
                .readFully(bytes, 0, length)
            `in`.close()
            return bytes
        } catch (e: Exception) {
            return null
        }
    }

    fun loadModelFromFile(fileName: String) {
        loadModelFromBytes(getBytesFromFile(fileName))
    }

    fun loadModelFromBytes(bytes: ByteArray?) {
        this.model = if (bytes != null) Model.decode(bytes, -1) else null
    }

    fun setModel(model: Model?) {
        this.model = model
    }

    private fun render() {
        renderer = ModelViewerRenderer(width, height)
    }

    private fun mouseWheelMovedCallback(var1: MouseWheelEvent) {
        val rotation = var1.wheelRotation
        axisZ += rotation * 50
    }

    fun mouseReleasedCallback(event: MouseEvent) {
        screenX = -1
        screenY = screenX
        val mouseType = event.button
        if (mouseType == 3) {
            posX = oriPosX
            posY = oriPosY
            axisX = 0
            axisY = 0
            axisZ = 500
        }
    }

    fun componentResizedCallback(c: ComponentEvent?) {
        if (width > 0 && height > 0) {
            render()
        }
    }

    fun mouseDraggedCallback(event: MouseEvent) {
        val x = event.x
        val y = event.y
        val button = InputEvent.getModifiersExText(event.modifiersEx)
        val mouseType = Character.getNumericValue(button[button.length - 1])
        if (mouseType == 1) {
            if (screenX != -1 || screenY != -1) {
                axisX += (screenX - x) * 3
                if (axisX < 0) {
                    axisX += 2048
                } else if (axisX >= 2048) {
                    axisX = 2048 - axisX
                }

                axisY -= (screenY - y) * 3
                if (axisY < 0) {
                    axisY += 2048
                } else if (axisY >= 2048) {
                    axisY = 2048 - axisY
                }
            }
        } else if (mouseType == 2) {
            if (screenX != -1 || screenY != -1) {
                posX += x - screenX
                if (posX < 0) {
                    posX += 2048
                } else if (posX >= 2048) {
                    posX = 2048 - posX
                }

                posY -= screenY - y
                if (posY < 0) {
                    posY += 2048
                } else if (posY >= 2048) {
                    posY = 2048 - posY
                }
            }
        }

        screenX = x
        screenY = y
    }

    companion object {
        private var gcDelay = 0L
    }
}
