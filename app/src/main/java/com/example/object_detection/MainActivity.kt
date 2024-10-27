package com.example.object_detection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.object_detection.ui.theme.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Object_detectionTheme {  // Wrap your app's content in the theme
                // Your app content here
                CameraGalleryScreen(
                    onLiveDetectionClick = { /* handle click */ },
                    onGalleryClick = { /* handle click */ }
                )
            }
        }
    }
}