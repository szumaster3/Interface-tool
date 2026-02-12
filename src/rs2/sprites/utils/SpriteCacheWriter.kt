package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import rs2.sprites.SpriteLoader

object SpriteCacheWriter {
    fun write(
        spriteId: Int,
        archive: SpriteArchive,
    ) {
        val data = archive.encode().array()
        SpriteLoader.put(spriteId, data)
        println("[SpriteCache] Sprite $spriteId written to cache (${data.size} bytes)")
    }
}
