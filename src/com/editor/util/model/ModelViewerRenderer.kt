package com.editor.util.model

import com.alex.loaders.mesh.Model
import com.alex.loaders.mesh.ModelRenderer
import java.awt.Graphics

/**
 * Renders a [Model] for the model viewer panel using [ModelRenderer].
 */
class ModelViewerRenderer(width: Int, height: Int) {
    private val renderer = ModelRenderer(width, height)

    fun render(model: Model, centerX: Int, centerY: Int, pitch: Int, yaw: Int, zoom: Int) {
        renderer.clear(BACKGROUND)
        renderer.render(model, centerX, centerY, pitch, yaw, 0, zoom, 0, 0, false)
    }

    fun draw(g: Graphics) {
        g.drawImage(renderer.image, 0, 0, null)
    }

    companion object {
        private val BACKGROUND = 0xFFB0B0B0L.toInt()
    }
}
