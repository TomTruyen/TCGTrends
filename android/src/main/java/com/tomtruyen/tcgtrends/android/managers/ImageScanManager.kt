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
    private const val ANALYSIS_INTERVAL = 1000L
    private var lastAnalysisTime: Long? = null

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val jpRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    private val _recognizedText = MutableStateFlow(null as Text?)
    val recognizedText = _recognizedText.asStateFlow()

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        if(!shouldAnalyze()) {
            image.close()
            return
        }

        lastAnalysisTime = System.currentTimeMillis()

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

    private fun shouldAnalyze(): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastAnalysisTime = lastAnalysisTime ?: 0

        return currentTime - lastAnalysisTime >= ANALYSIS_INTERVAL
    }

    // Filters out the text that we don´t want
    private fun filter() {

    }
}