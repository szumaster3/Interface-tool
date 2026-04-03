package rs2.editor.model

import com.alex.loaders.mesh.ModelData
import com.alex.loaders.mesh.Unknown1
import com.alex.loaders.mesh.Unknown2
import java.awt.Component
import java.awt.Graphics
import java.awt.Image
import java.awt.image.*

class ModelViewerRenderer(width: Int, height: Int, component: Component) : ImageObserver, ImageProducer {
    private val width: Int
    private val height: Int
    private val colorModel: ColorModel = DirectColorModel(32, 16711680, '\uff00'.code, 255)
    private val generated3DModelImage: Image
    private val screen: IntArray
    private val pixels: IntArray
    private var imageProducer: ImageConsumer? = null
    var lastAxisX = 0
    var lastAxisY = 0
    var lastAxisZ = 0

    init {
        Unknown1.setLighting(0.8)

        Unknown1.setDimensions(width, height)
        this.screen = Unknown1.screen
        this.width = width
        this.height = height
        this.pixels = IntArray(width * height)
        this.generated3DModelImage = component.createImage(this)

        component.prepareImage(this.generated3DModelImage, this)
        this.initScreenProperties()
    }

    private fun initScreenProperties() {
        val pixels = this.pixels
        var width = this.width
        var height = this.height
        ModelData.pixels = pixels
        ModelData.width = width
        ModelData.height = height
        if (width > ModelData.width) {
            width = ModelData.width
        }
        if (height > ModelData.height) {
            height = ModelData.height
        }
        ModelData.width2 = width
        ModelData.height2 = height
        ModelData.field65 = ModelData.width2 - 1
    }

    fun updatePreview(var1: Int, var2: Int, g: Graphics) {
        this.generatePixels()
        g.drawImage(this.generated3DModelImage, 0, 0, this)
    }

    @Synchronized
    override fun addConsumer(c: ImageConsumer) {
        this.imageProducer = c
        c.setDimensions(this.width, this.height)
        c.setProperties(null)
        c.setColorModel(this.colorModel)
        c.setHints(14)
    }

    @Synchronized
    override fun isConsumer(var1: ImageConsumer): Boolean {
        return this.imageProducer === var1
    }

    @Synchronized
    override fun removeConsumer(var1: ImageConsumer) {
        if (this.imageProducer === var1) {
            this.imageProducer = null
        }
    }

    override fun startProduction(var1: ImageConsumer) {
        this.addConsumer(var1)
    }

    override fun requestTopDownLeftRightResend(var1: ImageConsumer) {
    }

    @Synchronized
    private fun generatePixels() {
        imageProducer!!.setPixels(0, 0, this.width, this.height, this.colorModel, this.pixels, 0, this.width)
        imageProducer!!.imageComplete(2)
    }

    override fun imageUpdate(arg0: Image, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int): Boolean {
        return true
    }

    fun updateModel(model: Unknown2?, x: Int, axisX: Int, y: Int, axisY: Int, axisZ: Int) {
        var axisX = axisX
        var axisY = axisY
        var axisZ = axisZ

        this.initScreenProperties()
        Unknown1.screen = this.screen

        if (axisY < 0) axisY = 0
        if (axisX < 0) axisX = 0

        val var7 = Unknown1.field16[axisX] * axisZ shr 16
        axisZ = Unknown1.field17[axisX] * axisZ shr 16

        lastAxisX = axisX
        lastAxisY = axisY
        lastAxisZ = axisZ

        if (model != null) {
            model.setViewPoint(0, axisY, 0, axisX, 0, var7, axisZ)
            Unknown1.screenPosX = x
            Unknown1.screenPosY = y
        } else {
            throw NullPointerException("You cant render a null model!")
        }
    }

    fun clear() {
        this.initScreenProperties()
        val screen = Unknown1.width * Unknown1.height

        for (i in 0 until screen) {
            Unknown1.pixels!![i] = 0xB0B0B0
        }
    }
}
