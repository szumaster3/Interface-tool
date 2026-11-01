package com.alex.loaders.items

import com.alex.Cache
import com.alex.loaders.models.ModelLoader
import com.alex.loaders.models.ModelRenderer
import java.awt.image.BufferedImage

/**
 * Item names and inventory icons (36x32), drawn from the item model the same way the client does it.
 * Used to preview items in inventory components.
 */
object ItemIcons {

    const val WIDTH = 36
    const val HEIGHT = 32

    private val definitions = HashMap<Int, ItemDefinition?>()
    private val icons = HashMap<Int, BufferedImage?>()
    private var samples: List<Int>? = null

    fun definition(itemId: Int): ItemDefinition? = definitions.getOrPut(itemId) {
        val store = Cache.store
        if (store == null || itemId < 0 || store.indexes.size <= 19) {
            null
        } else {
            try {
                ItemDefinition.getItemDefinition(store, itemId).takeIf { it.loaded }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun name(itemId: Int): String = definition(itemId)?.name ?: "null"

    fun icon(itemId: Int): BufferedImage? = icons.getOrPut(itemId) { createIcon(itemId) }

    /**
     * A list of valid items which is used to fill the inventories when no item ids were chosen.
     */
    fun sampleItems(): List<Int> = samples ?: (1..4000).asSequence()
        .filter { id -> definition(id)?.let { it.name != "null" && it.model > 0 && it.certtemplate == -1 } == true }
        .take(40).toList().also { samples = it }

    fun clear() {
        definitions.clear()
        icons.clear()
        samples = null
    }

    private fun createIcon(itemId: Int): BufferedImage? {
        val item = definition(itemId) ?: return null
        // a noted item is drawn with the model of its template
        val certificate = item.certtemplate != -1
        val source = if (certificate) definition(item.certtemplate) ?: return null else item
        val baseModel = ModelLoader.getModel(source.model) ?: return null

        val model = baseModel.copy()
        item.recol_d?.let { from ->
            val to = item.recol_s
            for (i in from.indices) {
                model.recolor(from[i], to[i])
            }
        }
        if (source.resizeX != 128 || source.resizeY != 128 || source.resizeZ != 128) {
            model.resize(source.resizeX, source.resizeY, source.resizeZ)
        }
        model.relight(source.ambient + 64, source.contrast + 768)

        val zoom = if (certificate) (source.zoom2d * 1.5).toInt() else source.zoom2d
        val renderer = ModelRenderer(WIDTH, HEIGHT)
        renderer.clear(0)
        renderer.renderItem(
            model, 16, 16, source.xAngle2D, source.yAngle2D, source.zAngle2D, zoom, source.xOffset2D, source.yOffset2D
        )
        return renderer.image
    }

    /**
     * Parses `995, 4151, 10-20` into a list of item ids.
     */
    fun parseIds(text: String): List<Int> =
        text.split(',', ';', ' ').map { it.trim() }.filter { it.isNotEmpty() }.flatMap { part ->
            val range = part.split('-')
            val from = range.getOrNull(0)?.toIntOrNull()
            val to = if (range.size > 1) range[1].toIntOrNull() else from
            if (from != null && to != null && to >= from && to - from < 2000) (from..to).toList() else emptyList()
        }
}
