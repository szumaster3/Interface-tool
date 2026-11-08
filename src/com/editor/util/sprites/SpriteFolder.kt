package com.editor.util.sprites

import java.io.File

/**
 * Reads the layout of a folder of sprites, which is the layout "Export all" of the sprite manager writes.
 */
object SpriteFolder {

    /**
     * A sprite with a single frame is the file `sprite_<id>.png`, a sprite with more frames is the folder
     * `sprite_<id>` with the files `<id>_<frame>.png`.
     *
     * @return the frame files of every sprite, in the order of the frames, by the id of the sprite
     */
    fun findGroups(folder: File): Map<Int, List<File>> {
        val groups = sortedMapOf<Int, List<File>>()
        for (file in folder.listFiles().orEmpty()) {
            val name = file.name
            if (file.isFile && name.startsWith("sprite_") && name.endsWith(".png")) {
                name.removePrefix("sprite_").removeSuffix(".png").toIntOrNull()?.let { groups[it] = listOf(file) }
            } else if (file.isDirectory && name.startsWith("sprite_")) {
                val id = name.removePrefix("sprite_").toIntOrNull() ?: continue
                val frames = file.listFiles().orEmpty()
                    .filter { it.isFile && it.name.endsWith(".png") }
                    .mapNotNull { f -> f.name.removeSuffix(".png").substringAfterLast('_').toIntOrNull()?.let { it to f } }
                    .sortedBy { it.first }
                    .map { it.second }
                if (frames.isNotEmpty()) groups[id] = frames
            }
        }
        return groups
    }
}
