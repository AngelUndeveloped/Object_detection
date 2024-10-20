package com.example.object_detection

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

// ObjectDetectionHelper class: This class is responsible for loading the TensorFlow Lite model (yolo11n.pt) and providing methods to run inference on the model.)
class ObjectDetectionHelper(context: Context) {
    // Interpreter for TensorFlow Lite Model
    private var interpreter: Interpreter? = null
    // Initializer block: This block runs when the class is instantiated. It loads the model from assets and initializes the interpreter.
    init {
        // Load the model from assets
        val model = context.assets.open("yolo11n.pt").use {inputStream ->
            // Read the model into a byte array
            val modelBytes = ByteArray(inputStream.available())
            inputStream.read(modelBytes)

            // Allocate a direct ByteBuffer large enough to hold the model and store the bytes in it.
            ByteBuffer.allocateDirect(modelBytes.size).apply {
                // Ensure the byte buffer uses native byte order for consistent results across different platforms.
                order(ByteOrder.nativeOrder())
                // Store the model bytes in the allocated buffer.
                put(modelBytes)
            }
        }
        // Create a new TensorFlow Lite Interpreter with the loaded model
        interpreter = Interpreter(model)
    }

    // Function to run inference: Takes input data as a ByteBuffer and outputs the result as a ByteBuffer
    fun runInference(input: ByteBuffer, output: ByteBuffer) {
        // Use the interpreter to run the input through the model, filling the output buffer with the results
        interpreter?.run(input, output)
    }

    // Function to close the interpreter and release resources when it is no longer needed.
    fun close() {
        interpreter?.close()
    }
}