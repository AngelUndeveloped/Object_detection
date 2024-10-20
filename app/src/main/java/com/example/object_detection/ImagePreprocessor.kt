package com.example.object_detection

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ImagePreprocessor class: This class is responsible for preparing a Bitmap image into the correct
 * format for input into the TensorFlow Lite model
 *
 */
class ImagePreprocessor {
    /**
     * Takes a Bitmap and converts it into a ByteBuffer suitable for model input.
     * The input size is specified by the model
     */
    fun preprocessImage(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        /**
         * create a Bytebuffer to hold the image data
         * Each pixel has 3 channels (RGB) and each channel is represented as a 32-bit float (4 bytes)
         * We allocate 4 bytes for each of the inputSize * inputSize * 3 channels
         */
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)

        // Set the byte order to native (endianness of the current platform), as required by tensorflow lite
        byteBuffer.order(ByteOrder.nativeOrder())

        /**
         * Create an integer array to hold the pixel values from the bitmap
         * The size of the array is the number of pixels (inputSize x inputSize)
         */
        val intValues = IntArray(inputSize * inputSize)

        // Get pixel data from the bitmap and store it in the integer array.
        // Each int in the array represent a pixel, encoded as 0xAARRGGBB (Alpha, Red, Green, Blue).
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Loop through each pixel in the image to extract the RGB values and normalize them.
        for ( pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            // Extract the green component (shift by 8 bits and mask with 0xFF)
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            // Extract the blue component (mask with 0xFF)
            val b = (pixelValue and 0xFF) / 255.0f

            // Add the normalized values for red, green and blue into the ByteBuffer
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }
}