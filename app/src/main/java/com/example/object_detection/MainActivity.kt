package com.example.object_detection

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions


class MainActivity : ComponentActivity() {
    /**
     * Register a photo picker activity launcher in single-select mode.
     */
    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected UIR: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    /**
     * Create a local model object from the model file path
     */
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("yolo11n.pt")
        // or .setAbsoluteFilePath(absolute file path to model file)
        // or .setUri(URI to model file)
        .build()

    /**
     * Create an object detector option builder with the local model.
     * Live detection and tracking
     */
    private val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()



    /**
     * Multiple object detection in static images
     */
    /*val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()*/

    val objectDetector =
        ObjectDetection.getClient(customObjectDetectorOptions)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }
}
