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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.tomtruyen.tcgtrends.android.R
import com.tomtruyen.tcgtrends.android.managers.ImageScanManager
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

@Composable
fun ImageScanScreen() {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }

    val scanManager = remember { ImageScanManager() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted) {

        } else {
            // Show Dialog
        }
    }

    LaunchedEffect(Unit) {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

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
                cameraController.unbind()
                cameraController.bindToLifecycle(lifecycleOwner)

                // TODO: Find way to check if camera is active
                cameraController.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object: ImageCapture.OnImageCapturedCallback() {
                        @ExperimentalGetImage
                        override fun onCaptureSuccess(image: ImageProxy) {
                            scope.launch {
                                scanManager.process(image) {
                                    Log.e("@@@", it ?: "Unknown Scan Error")
                                }?.let { result ->
                                    Log.d("@@@", result.text)
                                }

                                image.close()
                            }
                        }
                    }
                )
            }
        },
        onRelease = {
            cameraController.unbind()
        }
    )
}