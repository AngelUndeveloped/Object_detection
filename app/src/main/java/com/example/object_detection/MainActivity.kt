package com.example.object_detection

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.object_detection.ui.theme.Object_detectionTheme


class MainActivity : ComponentActivity() {

    var sharedSelectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Object_detectionTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        CameraGalleryScreen(
                            onLiveDetectionClick = {
                                navController.navigate("live_detection")
                            },
                            onGalleryClick = {
                                navController.navigate("gallery")
                            }
                        )
                    }
                    composable("gallery") {
                        GalleryScreen()
                    }
                    composable("live_detection") {
                        // We'll implement this later
                    }
                }
            }
        }
    }
}