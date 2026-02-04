package com.interfaces.text

import com.rs.cache.loaders.ComponentDefinition
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object FontDecoding {

    fun getTextArray(component: ComponentDefinition): Array<BufferedImage?> {
        val text = component.text.orEmpty()
        val arr = arrayOfNulls<BufferedImage>(text.length)

        for ((index, c) in text.withIndex()) {
            try {
                val filePath = "dumps/${component.fontId}_${getCharIndex(c)}.png"
                arr[index] = ImageIO.read(File(filePath))
            } catch (e: IOException) {
            }
        }

        return arr
    }

    private fun getCharIndex(c: Char): Int = when {
        c.isDigit() -> 48 + c.digitToInt()
        c.isUpperCase() -> 55 + c.digitToInt()
        c.isLowerCase() -> 87 + c.digitToInt()
        else -> 0
    }
}
