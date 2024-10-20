package com.example.object_detection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : ComponentActivity() {

    // Declare an ImageView to display the selected image from the gallery.
    private lateinit var selectedImageView: ImageView

    // onCreate: This method is called when the activity is first created.
    // It sets up the UI and opens the gallery for the user to pick an image.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity layout to activity_main.xml which defines the UI.
        setContentView(R.layout.activity_main)

        // Find the ImageView in the layout to show the selected image.
        selectedImageView = findViewById(R.id.selectedImageView)

        // Create an Intent to open the gallery and allow the user to pick an image.
        // Intent.ACTION_PICK specifies that the user will be allowed to pick a piece of data (image).
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Start the activity and wait for the result (selected image) with request code 1.
        startActivityForResult(galleryIntent, 1)
    }

    // onActivityResult: This method is called when the user has selected an image and the result is returned.
    // Here, we get the image, display it, and run object detection.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the request code matches (1 for gallery selection) and the result is OK.
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get the URI of the selected image from the returned Intent.
            val imageUri: Uri? = data?.data

            // Convert the image URI to a Bitmap using MediaStore.
            val imageBitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

            // Display the selected image in the ImageView.
            selectedImageView.setImageBitmap(imageBitmap)

            // Run object detection on the selected image (this is where we will add the object detection code).
            runObjectDetection(imageBitmap)
        }
    }

    private fun runObjectDetection(imageBitmap: Bitmap) {
        // Initialize the ImagePreprocessor class, which will preprocess the image for the model.
        val imagePreprocessor = ImagePreprocessor()

        // Preprocess the image: Convert the bitmap into a ByteBuffer in the required format for the model.
        // Here, we assume the model expects input of size 300x300 pixels.
        val inputBuffer = imagePreprocessor.preprocessImage(
            imageBitmap,
            300
        ) // Assuming model input size is 300x300

        // Allocate a ByteBuffer to hold the output from the model.
        // Here, we assume that the model outputs 10 detections, and each detection consists of 4 float values
        // (usually for bounding box coordinates, i.e., x, y, width, height). The buffer size is 4 bytes per float,
        // 10 detections, and 4 floats per detection.
        val outputBuffer =
            ByteBuffer.allocateDirect(4 * 10 * 4) // Assuming model outputs 10 detections (adjust as needed)

        // Set the byte order of the output buffer to native order (the endianness of the current platform).
        outputBuffer.order(ByteOrder.nativeOrder())

        // Initialize the ObjectDetectionHelper class, which manages the TensorFlow Lite interpreter.
        // The 'this' keyword refers to the context, which is the current activity.
        val objectDetectionHelper = ObjectDetectionHelper(this)

        // Run inference: Use the interpreter to process the input buffer (preprocessed image)
        // and write the results to the output buffer.
        objectDetectionHelper.runInference(inputBuffer, outputBuffer)

        // TODO: Post-process the output buffer and display results.
        // The raw model output is now in outputBuffer. You will need to extract the detection results (bounding boxes,
        // confidence scores, class labels, etc.) from the buffer, interpret them, and then display them in the UI.
        postProcessOutput(outputBuffer, imageBitmap)
    }

    private fun postProcessOutput(outputBuffer: ByteBuffer, imageBitmap: Bitmap) {
        outputBuffer.rewind() // Reset the buffer's position to the start

        // Set up Paint object for drawing bounding boxes
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        // Create a mutable copy of the original bitmap to draw on
        val mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Iterate over the detections (assuming 10 detections and 6 values per detection)
        for (i in 0 until 10) {
            // Extract the bounding box coordinates, confidence score, and class label from the buffer
            val x = outputBuffer.float   // X coordinate of the bounding box
            val y = outputBuffer.float   // Y coordinate of the bounding box
            val width = outputBuffer.float   // Width of the bounding box
            val height = outputBuffer.float   // Height of the bounding box
            val confidence = outputBuffer.float   // Confidence score
            val classLabel =
                outputBuffer.float   // Class label (we assume it's a float value representing an index)

            // Filter out low-confidence detections (e.g., confidence less than 0.5)
            if (confidence < 0.5) {
                continue
            }

            // Log the detection info (optional)
            Log.d(
                "ObjectDetection",
                "Detection $i -> Box: [$x, $y, $width, $height], Confidence: $confidence, ClassLabel: $classLabel"
            )

            // Calculate the bounding box coordinates relative to the original image size
            val left = x * imageBitmap.width
            val top = y * imageBitmap.height
            val right = left + (width * imageBitmap.width)
            val bottom = top + (height * imageBitmap.height)

            // Draw the bounding box on the canvas
            canvas.drawRect(left, top, right, bottom, paint)
        }

        // Update the ImageView with the image containing the b
    }

}