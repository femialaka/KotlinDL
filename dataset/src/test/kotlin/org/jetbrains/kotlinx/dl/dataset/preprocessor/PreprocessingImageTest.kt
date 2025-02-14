package org.jetbrains.kotlinx.dl.dataset.preprocessor

import org.jetbrains.kotlinx.dl.api.extension.set3D
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.InterpolationType
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.crop
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.resize
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.rotate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage

class PreprocessingImageTest {
    @Test
    fun resizeTest() {
        val preprocess = preprocess {
            load {
                colorMode = ColorOrder.BGR
            }
            transformImage {
                resize {
                    outputWidth = 4
                    outputHeight = 4
                    interpolation = InterpolationType.NEAREST
                }
            }
            transformTensor {
                rescale { }
            }
        }
        val inputImage = BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR)
        inputImage.setRGB(0, 0, Color.BLUE.rgb)
        inputImage.setRGB(1, 1, Color.RED.rgb)
        val (imageFloats, imageShape) = preprocess.handleImage(inputImage, "test")
        Assertions.assertEquals(ImageShape(4, 4, 3), imageShape)
        val expectedImage = FloatArray(imageShape.numberOfElements.toInt()) { 0f }.apply {
            for (i in 0..1)
                for (j in 0..1)
                    setRGB(i, j, Color.BLUE, imageShape, ColorOrder.BGR)
            for (i in 2..3)
                for (j in 2..3)
                    setRGB(i, j, Color.RED, imageShape, ColorOrder.BGR)
        }
        Assertions.assertArrayEquals(expectedImage, imageFloats)
    }

    @Test
    fun cropTest() {
        val preprocess = preprocess {
            load {
                colorMode = ColorOrder.BGR
            }
            transformImage {
                crop {
                    left = 1
                    right = 0
                    top = 0
                    bottom = 1
                }
            }
            transformTensor {
                rescale { }
            }
        }
        val inputImage = BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR)
        inputImage.setRGB(0, 0, Color.BLUE.rgb)
        inputImage.setRGB(1, 0, Color.GREEN.rgb)
        inputImage.setRGB(1, 1, Color.RED.rgb)
        inputImage.setRGB(0, 1, Color.GREEN.rgb)

        val (imageFloats, imageShape) = preprocess.handleImage(inputImage, "test")
        Assertions.assertEquals(ImageShape(1, 1, 3), imageShape)

        val expectedImage = FloatArray(3).apply { setRGB(0, 0, Color.GREEN, imageShape, ColorOrder.BGR) }
        Assertions.assertArrayEquals(expectedImage, imageFloats)
    }

    @Test
    fun rotateTest() {
        val preprocess = preprocess {
            load {
                colorMode = ColorOrder.BGR
            }
            transformImage {
                rotate {
                    degrees = 90f
                }
            }
            transformTensor {
                rescale { }
            }
        }
        val inputImage = BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR)
        inputImage.setRGB(0, 0, Color.BLUE.rgb)
        inputImage.setRGB(1, 1, Color.RED.rgb)
        val (imageFloats, imageShape) = preprocess.handleImage(inputImage, "test")

        Assertions.assertEquals(ImageShape(2, 2, 3), imageShape)
        val expectedImage = FloatArray(imageShape.numberOfElements.toInt()) { 0f }
        expectedImage.setRGB(1, 0, Color.BLUE, imageShape, ColorOrder.BGR)
        expectedImage.setRGB(0, 1, Color.RED, imageShape, ColorOrder.BGR)
        Assertions.assertArrayEquals(expectedImage, imageFloats)
    }

    private fun FloatArray.setRGB(x: Int, y: Int, color: Color, imageShape: ImageShape, colorOrder: ColorOrder) {
        val colorComponents = when (colorOrder) {
            ColorOrder.RGB -> floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f)
            ColorOrder.BGR -> floatArrayOf(color.blue / 255f, color.green / 255f, color.red / 255f)
        }
        for (i in colorComponents.indices) {
            set3D(y, x, i, imageShape.width!!.toInt(), imageShape.channels.toInt(), colorComponents[i])
        }
    }
}