package com.tomtruyen.tcgtrends.android.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tomtruyen.tcgtrends.android.Dimens
import com.tomtruyen.tcgtrends.android.R
import com.tomtruyen.tcgtrends.android.managers.ImageScanManager
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

@Composable
fun ImageScanScreen() {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), ImageScanManager)
        }
    }

    val recognizedText by ImageScanManager.recognizedText.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {
            // Show Dialog
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(recognizedText) {
        Log.d("@@@", "Recognized Text: ${recognizedText?.text}")
    }


    // TODO: Add a clipped box for the camera - See if we can do this with a modifier
    // TODO: When we got that then we can get the Canvas from it and use ImageScanManager.updateTransform?? Maybe
    Box(
        modifier = Modifier.fillMaxSize()
            .drawWithContent {
                // Aspect Ratio of a Pokemon Card
                val cardAspectRatio = 63f / 88f // 6.3cm x 8.8cm

                // Padding around the canvas
                val canvasPadding = Dimens.medium.toPx()

                // Canvas dimensions
                val canvasWidth = size.width - canvasPadding * 2
                val canvasHeight = size.height - canvasPadding * 2

                // Aspect Ratio of the canvas
                val aspectRatio = canvasWidth / canvasHeight

                // Calculation of the width and height of the scan area based on the aspect ratio of the card
                val width: Float
                val height: Float

                if (aspectRatio > cardAspectRatio) {
                    // If the canvas is wider than the cardAspectRatio
                    width = canvasHeight * cardAspectRatio
                    height = canvasHeight
                } else {
                    // If the canvas is taller than the cardAspectRatio
                    width = canvasWidth
                    height = canvasWidth / cardAspectRatio
                }

                val offsetX = (canvasWidth - width) / 2 + canvasPadding
                val offsetY = (canvasHeight - height) / 2 + canvasPadding

                val rectOffset = Offset(offsetX, offsetY)

                drawContent()

                drawWithLayer {
                    // Area outside of the scan area
                    drawRect(Color(0x99000000))

                    // Scan area
                    drawRect(
                        topLeft = rectOffset,
                        size = Size(width, height),
                        color = Color.Transparent,
                        blendMode = BlendMode.SrcIn
                    )
                }

                // Border around the scan area
                drawRect(
                    topLeft = rectOffset,
                    size = Size(width, height),
                    color = Color.White,
                    style = Stroke(2.dp.toPx())
                )
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )
    }
}

/**
 * Draw with layer to use [BlendMode]s
 */
private fun DrawScope.drawWithLayer(block: DrawScope.() -> Unit) {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)
        block()
        restoreToCount(checkPoint)
    }
}