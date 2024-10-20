package com.example.object_detection

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : ComponentActivity() {

    // Use the new API to start an activity and receive the result
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    // Extract the image URI from the intent
                    val imageUri: Uri? = data.data
                    if (imageUri != null) {
                        val imageBitmap: Bitmap? =
                            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        if (imageBitmap != null) {
                            selectedImageView.setImageBitmap(imageBitmap)
                            runObjectDetection(imageBitmap)
                        } else {
                            Log.e("MainActivity", "Failed to decode bitmap from URI: $imageUri")
                        }
                    } else {
                        Log.e("MainActivity", "Image URI is null.")
                    }
                }
            }
        }

    // Declare an ImageView to display the selected image from the gallery
    private lateinit var selectedImageView: ImageView

    // onCreate: Initializes the UI and opens the gallery for the user to pick an image
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the ImageView in the layout
        selectedImageView = findViewById(R.id.selectedImageView)

        // Intent to open the gallery to select an image
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Launch the intent and wait for the result (image selection)
        startForResult.launch(galleryIntent)
    }

    // Run object detection on the selected image
    private fun runObjectDetection(imageBitmap: Bitmap) {
        // Initialize the ImagePreprocessor class to preprocess the image for the model
        val imagePreprocessor = ImagePreprocessor()
        // Preprocess the image to convert it into a ByteBuffer suitable for the model
        val inputBuffer = imagePreprocessor.preprocessImage(
            imageBitmap,
            300
        ) // Assuming model input size is 300x300

        // Allocate a ByteBuffer for the model's output
        val outputBuffer = ByteBuffer.allocateDirect(4 * 10 * 4) // 10 detections (adjust as needed)
        outputBuffer.order(ByteOrder.nativeOrder()) // Set byte order to native

        // Initialize ObjectDetectionHelper to run inference using the model
        val objectDetectionHelper = ObjectDetectionHelper(this)
        objectDetectionHelper.runInference(inputBuffer, outputBuffer)

        // Post-process the output buffer and display the results
        postProcessOutput(outputBuffer, imageBitmap)
    }

    // Post-process the output buffer, extract detection results, and display them
    private fun postProcessOutput(outputBuffer: ByteBuffer, imageBitmap: Bitmap) {
        outputBuffer.rewind() // Reset buffer position to the start

        // Set up Paint object to draw bounding boxes on the image
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        // Create a mutable copy of the original bitmap to draw on
        val mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Iterate over the detections (assuming 10 detections, 6 values per detection)
        for (i in 0 until 10) {
            // Extract bounding box coordinates and other detection details from the output buffer
            val x = outputBuffer.float // X coordinate
            val y = outputBuffer.float // Y coordinate
            val width = outputBuffer.float // Width of the bounding box
            val height = outputBuffer.float // Height of the bounding box
            val confidence = outputBuffer.float // Confidence score
            val classLabel = outputBuffer.float // Class label (index)

            // Only process detections with confidence greater than 0.5
            if (confidence < 0.5) continue

            // Log the detection information
            Log.d(
                "ObjectDetection",
                "Detection $i -> Box: [$x, $y, $width, $height], Confidence: $confidence, ClassLabel: $classLabel"
            )

            // Calculate the bounding box coordinates relative to the image size
            val left = x * imageBitmap.width
            val top = y * imageBitmap.height
            val right = left + (width * imageBitmap.width)
            val bottom = top + (height * imageBitmap.height)

            // Draw the bounding box on the image
            canvas.drawRect(left, top, right, bottom, paint)
        }

        // Update the ImageView with the image containing the drawn bounding boxes
        selectedImageView.setImageBitmap(mutableBitmap)
    }
}
