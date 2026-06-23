package com.editor.utils

import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import java.awt.image.ImageFilter
import java.awt.image.RGBImageFilter

/**
 * Created by Kyle Fricilone on Jun 15, 2017.
 */
object ImageUtils {
    @JvmStatic
    fun horizontalFlip(img: BufferedImage): BufferedImage {
        val w = img.width
        val h = img.height
        val flippedImage = BufferedImage(w, h, img.type)
        val g = flippedImage.createGraphics()
        g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null)
        g.dispose()
        return flippedImage
    }

    @JvmStatic
    fun verticalFlip(img: BufferedImage): BufferedImage {
        val w = img.width
        val h = img.height
        val flippedImage = BufferedImage(w, h, img.colorModel.transparency)
        val g = flippedImage.createGraphics()
        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null)
        g.dispose()
        return flippedImage
    }

    fun getScaledImage(
        src: BufferedImage,
        factor: Double,
    ): BufferedImage {
        var factor = factor
        var finalw = src.width
        var finalh = src.height
        if (src.width > src.height) {
            factor = (src.height.toDouble() / src.width.toDouble())
            finalh = (finalw * factor).toInt()
        } else {
            factor = (src.width.toDouble() / src.height.toDouble())
            finalw = (finalh * factor).toInt()
        }

        val resizedImg = BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT)
        val g2 = resizedImg.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2.drawImage(src, 0, 0, finalw, finalh, null)
        g2.dispose()
        return resizedImg
    }

    fun makeColorTransparent(
        im: BufferedImage,
        color: Color,
    ): BufferedImage {
        val filter: ImageFilter =
            object : RGBImageFilter() {
                var markerRGB: Int = color.rgb or -0x1000000

                override fun filterRGB(
                    x: Int,
                    y: Int,
                    rgb: Int,
                ): Int =
                    if ((rgb or -0x1000000) == markerRGB) {
                        0x00FFFFFF and rgb
                    } else {
                        rgb
                    }
            }

        return imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(FilteredImageSource(im.source, filter)))
    }

    @JvmStatic
    fun imageToBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }

        val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val bGr = bimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()
        return bimage
    }

    fun convert(
        src: BufferedImage,
        bufImgType: Int,
    ): BufferedImage {
        val img = BufferedImage(src.width, src.height, bufImgType)
        val g2d = img.createGraphics()
        g2d.drawImage(src, 0, 0, null)
        g2d.dispose()
        return img
    }

    fun createColoredBackground(
        image: BufferedImage,
        color: Color?,
    ): BufferedImage {
        val copy = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = copy.createGraphics()
        g2d.color = color
        g2d.fillRect(0, 0, copy.width, copy.height)
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        return copy
    }

    @JvmStatic
    fun resize(
        img: BufferedImage,
        newW: Int,
        newH: Int,
    ): BufferedImage {
        var newW = newW
        var newH = newH
        if (newW <= 0) newW = img.width
        if (newH <= 0) newH = img.height
        val tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH)
        val dimg = BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB)
        val g2d = dimg.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()
        return dimg
    }

    @JvmStatic
    fun colorImage(
        image: BufferedImage,
        col: Color,
    ): BufferedImage {
        val width = image.width
        val height = image.height
        val raster = image.raster

        for (xx in 0 until width) {
            for (yy in 0 until height) {
                val pixels = raster.getPixel(xx, yy, null as IntArray?)
                pixels[0] = col.red
                pixels[1] = col.green
                pixels[2] = col.blue
                raster.setPixel(xx, yy, pixels)
            }
        }
        return image
    }

    @JvmStatic
    fun rgbToRS2HSB(
        red: Int,
        green: Int,
        blue: Int,
    ): Int {
        val hsb = Color.RGBtoHSB(red, green, blue, null)
        val hue = (hsb[0] * 63).toInt()
        val sat = (hsb[1] * 7).toInt()
        val bri = (hsb[2] * 127).toInt()
        return (hue shl 10) + (sat shl 7) + bri
    }

    @JvmStatic
    fun rs2hsbToRGB(rs2hsb: Int): Int {
        val hue = rs2hsb shr 10 and 0x3F
        val sat = rs2hsb shr 7 and 0x7
        val bri = rs2hsb and 0x7F
        return Color.HSBtoRGB(hue / 63.0f, sat / 7.0f, bri / 127.0f)
    }
}
