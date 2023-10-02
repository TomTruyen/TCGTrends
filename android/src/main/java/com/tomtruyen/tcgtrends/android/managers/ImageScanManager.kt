package com.tomtruyen.tcgtrends.android.managers

import android.media.Image
import android.view.Surface
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.interfaces.Detector.TYPE_TEXT_RECOGNITION
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class ImageScanManager {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val jpRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @ExperimentalGetImage
    suspend fun process(imageProxy: ImageProxy, onFailure: (String?) -> Unit): Text? = withContext(Dispatchers.IO) {
        imageProxy.image?.let { image ->
            val imageRotation = imageProxy.imageInfo.rotationDegrees

            // TODO: Find a way to detect language of text

            val task = CompletableFuture.supplyAsync {
                recognizer.process(image, imageRotation)
            }

            // TODO: Use jpRecognizer if the text is in Japanese

            val taskResult = task.get()

            if(!taskResult.isSuccessful) {
                onFailure(taskResult.exception?.message)
                return@withContext null
            }

            taskResult.result
        }
    }

    // Filters out the text that we don´t want
    private fun filter() {

    }
}