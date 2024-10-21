package com.example.object_detection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia

class ImageSelector (private val context: Context, private val launcher: ActivityResultLauncher<PickVisualMedia.ImageOnly>) {

    //Method to start the gallery image selection
    fun selectImageFromGallery() {
        launcher.launch(PickVisualMedia.ImageOnly)
    }

    // Method to handle the result of the image selected and return the selected image as a Bitmap
    fun handleGalleryResult(result: ActivityResult, callback: (Bitmap?) -> Unit) {
        if (result.resultCode == Activity.RESULT_OK) {
             val imageUri: Uri? = result.data?.data
            imageUri?.let {
                val imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                callback(imageBitmap)
            } ?: callback(null)
        } else {
            callback(null) // In case of failure or cancellation
        }
    }
}