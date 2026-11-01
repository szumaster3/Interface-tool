package com.alex.loaders.fonts

import com.alex.Cache

/**
 * Loads the fonts of the cache and keeps them.
 */
object CacheFonts {

    private const val METRICS_INDEX = 13
    private const val SPRITES_INDEX = 8

    private val fonts = HashMap<Int, CacheFont?>()
    private var nameIcons: List<SpriteFrame?>? = null
    private var nameIconsLoaded = false

    /**
     * @return the font or null when the cache has no font with this id.
     */
    @Synchronized
    fun get(fontId: Int): CacheFont? {
        if (fontId < 0) return null
        return fonts.getOrPut(fontId) { load(fontId) }
    }

    /**
     * Ids of all the fonts of the cache.
     */
    @Synchronized
    fun ids(): List<Int> {
        val indexes = Cache.store?.indexes ?: return emptyList()
        if (indexes.size <= METRICS_INDEX || indexes[METRICS_INDEX] == null) return emptyList()
        val metrics = indexes[METRICS_INDEX]
        return (0..metrics.lastArchiveId).filter { metrics.archiveExists(it) && get(it) != null }
    }

    @Synchronized
    fun clear() {
        fonts.clear()
        nameIcons = null
        nameIconsLoaded = false
    }

    private fun load(fontId: Int): CacheFont? {
        try {
            val indexes = Cache.store?.indexes ?: return null
            if (indexes.size <= METRICS_INDEX || indexes[METRICS_INDEX] == null || indexes[SPRITES_INDEX] == null) {
                return null
            }
            val metricsIndex = indexes[METRICS_INDEX]
            val spritesIndex = indexes[SPRITES_INDEX]
            if (!metricsIndex.archiveExists(fontId) || !spritesIndex.archiveExists(fontId)) return null

            val metrics = metricsIndex.getFile(fontId, 0) ?: return null
            val glyphs = SpriteFrames.decode(spritesIndex.getFile(fontId, 0) ?: return null) ?: return null
            if (metrics.size < 257) return null
            return CacheFont(fontId, metrics, glyphs, nameIcons())
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * The icons which can be used in a text with `<img=n>` (for example the icons of the moderators).
     */
    private fun nameIcons(): List<SpriteFrame?>? {
        if (nameIconsLoaded) return nameIcons
        nameIconsLoaded = true
        try {
            val sprites = Cache.store?.indexes?.getOrNull(SPRITES_INDEX) ?: return null
            val archive = sprites.getArchiveId("name_icons")
            if (archive >= 0) {
                nameIcons = SpriteFrames.decode(sprites.getFile(archive, 0) ?: return null)
            }
        } catch (e: Exception) {
            nameIcons = null
        }
        return nameIcons
    }
}
