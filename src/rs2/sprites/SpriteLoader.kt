package rs2.sprites

import com.alex.store.Index
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import properties.Constants
import rs2.cache.Cache
import rs2.sprites.SpriteArchive.Companion.decode

object SpriteLoader {

    private val spriteCache: HashMap<Int, SpriteArchive> = HashMap()

    val numSprites: Int
        get() = spriteIndex.lastArchiveId + 1

    fun getArchive(archiveId: Int): SpriteArchive? {
        spriteCache[archiveId]?.let { return it }

        val data = spriteIndex.getFile(archiveId, 0) ?: return null
        val archive = decode(ByteBuffer.wrap(data)) ?: return null

        spriteCache[archiveId] = archive
        return archive
    }

    @JvmStatic
    fun getSprite(archiveId: Int): BufferedImage? =
        getSprite(archiveId, 0)

    fun getSprite(archiveId: Int, frame: Int): BufferedImage? =
        getArchive(archiveId)?.getSprite(frame)

    /**
     * Returns raw sprite data (file 0).
     * Used for export / re-decoding.
     */
    fun getRaw(archiveId: Int): ByteBuffer? {
        val data = spriteIndex.getFile(archiveId, 0) ?: return null
        return ByteBuffer.wrap(data)
    }

    /**
     * Writes sprite to cache.
     * Sprites use a single file per archive (fileId = 0).
     */
    fun put(archiveId: Int, data: ByteArray): Boolean {
        val success = spriteIndex.putFile(
            archiveId,
            0,
            data
        )

        if (success) {
            spriteCache.remove(archiveId)
            println("[SpriteLoader] Sprite $archiveId written to cache (${data.size} bytes)")
        } else {
            println("[SpriteLoader] Failed to write sprite $archiveId")
        }

        return success
    }

    /**
     * Clears cached sprite archive.
     */
    fun invalidate(archiveId: Int) {
        spriteCache.remove(archiveId)
        println("[SpriteLoader] Sprite $archiveId cache invalidated")
    }

    private val spriteIndex: Index
        get() = Cache.store!!.indexes[Constants.SPRITES_INDEX]
}
