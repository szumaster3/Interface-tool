package rs2.sprites

import com.alex.store.Index
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import properties.Constants
import rs2.cache.Cache
import rs2.sprites.SpriteArchive.Companion.decode

object SpriteLoader {
    var spriteCache: HashMap<Int, SpriteArchive> = HashMap()

    val numSprites: Int
        get() {
            val spriteIndex = spriteIndex
            return spriteIndex.lastArchiveId + 1
        }

    fun getArchive(archive: Int): SpriteArchive? {
        if (spriteCache.containsKey(archive)) {
            return spriteCache[archive]
        }

        val idx = spriteIndex
        val spriteData = idx.getFile(archive) ?: return null

        val spriteBuff = ByteBuffer.wrap(spriteData)
        val s = decode(spriteBuff) ?: return null

        spriteCache[archive] = s
        return s
    }

    @JvmStatic
    fun getSprite(archive: Int): BufferedImage? {
        return getSprite(archive, 0)
    }

    fun getSprite(archive: Int, fileIndex: Int): BufferedImage? {
        val arch = getArchive(archive) ?: return null

        return arch.getSprite(fileIndex)
    }

    val spriteIndex: Index
        get() = Cache.store!!.indexes[Constants.SPRITES_INDEX]
}
