package rs2.editor.sprites

import com.alex.Cache
import com.alex.loaders.sprites.SpriteArchive
import com.alex.loaders.sprites.SpriteArchive.Companion.decode
import com.alex.store.Index
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

object SpriteLoader {
    private val spriteCache: HashMap<Int, SpriteArchive> = HashMap()

    val numSprites: Int
        get() = spriteIndex.lastArchiveId + 1

    private fun getArchive(archiveId: Int): SpriteArchive? {
        spriteCache[archiveId]?.let { return it }

        val data = spriteIndex.getFile(archiveId, 0) ?: return null
        val archive = decode(ByteBuffer.wrap(data)) ?: return null

        spriteCache[archiveId] = archive
        return archive
    }

    @JvmStatic
    fun getSprite(archiveId: Int): BufferedImage? = getSprite(archiveId, 0)

    private fun getSprite(archiveId: Int, frame: Int): BufferedImage? = getArchive(archiveId)?.getSprite(frame)

    fun getRaw(archiveId: Int): ByteBuffer? {
        val data = spriteIndex.getFile(archiveId, 0) ?: return null
        return ByteBuffer.wrap(data)
    }

    fun put(archiveId: Int, data: ByteArray): Boolean {
        val success =
            spriteIndex.putFile(archiveId, 0, data)

        if (success) {
            spriteCache.remove(archiveId)
            println("[SpriteLoader] Sprite $archiveId written to cache (${data.size} bytes)")
        } else {
            println("[SpriteLoader] Failed to write sprite [$archiveId]")
        }

        return success
    }

    fun removeArchive(archiveId: Int): Boolean {
        spriteCache.remove(archiveId)
        val success = spriteIndex.putFile(archiveId, 0, ByteArray(0))

        if (success) {
            println("[SpriteLoader] Sprite $archiveId removed from cache")
        } else {
            println("[SpriteLoader] Failed to remove sprite [$archiveId]")
        }
        return success
    }

    private val spriteIndex: Index
        get() = Cache.getStore()!!.indexes[8]
}
