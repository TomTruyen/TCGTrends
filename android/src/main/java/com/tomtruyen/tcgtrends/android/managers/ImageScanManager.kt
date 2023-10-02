package com.tomtruyen.tcgtrends.android.managers

import android.media.Image
import android.view.Surface
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.interfaces.Detector.TYPE_TEXT_RECOGNITION
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ImageScanManager: ImageAnalysis.Analyzer {
    private val _recognizedText = MutableStateFlow(null as Text?)
    val recognizedText = _recognizedText.asStateFlow()

    val imageExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val jpRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        image.image?.let {
            val rotation = image.imageInfo.rotationDegrees

           recognizer.process(it, rotation).addOnCompleteListener { task ->
               if(task.isSuccessful) {
                   _recognizedText.tryEmit(task.result)
               }

               image.close()
           }
        }

    }

    // Filters out the text that we don´t want
    private fun filter() {

    }
}