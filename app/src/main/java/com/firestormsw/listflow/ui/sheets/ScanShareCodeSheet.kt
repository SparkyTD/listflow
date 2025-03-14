package com.firestormsw.listflow.ui.sheets

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.firestormsw.listflow.ui.components.SheetDragHandle
import com.firestormsw.listflow.ui.components.StyledButton
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.Background
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary
import com.firestormsw.listflow.ui.theme.Typography
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanShareCodeSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCodeScanned: (String) -> Unit,
) {
    if (!isOpen) {
        return
    }

    val configuration = LocalConfiguration.current
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a mutable state to track if camera should be bound
    var isCameraActive by remember { mutableStateOf(false) }

    // Remember camera provider and executor
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(localContext) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Store reference to the camera provider once we have it
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Get camera provider when sheet is opened
    LaunchedEffect(true) {
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            isCameraActive = true
        }, ContextCompat.getMainExecutor(localContext))
    }

    // Cleanup resources when the component is removed from composition
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
        SideEffect {
            cameraPermissionState.run { launchPermissionRequest() }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = {
                // Make sure to unbind camera uses cases when dismissing the sheet
                isCameraActive = false
                cameraProvider?.unbindAll()
                onDismiss()
            },
            dragHandle = { SheetDragHandle() },
            sheetState = SheetState(
                skipPartiallyExpanded = true,
                density = Density(localContext)
            ),
            containerColor = Background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Scan the QR code of a shared list",
                        style = Typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(configuration.screenWidthDp.dp.div(1f / 0.7f))
                        .padding(vertical = 0.dp)
                ) {
                    if (cameraPermissionState.status.shouldShowRationale) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "The camera permission was denied",
                                textAlign = TextAlign.Center,
                                style = Typography.bodyLarge,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        Surface {
                            // Only show camera when active
                            if (isCameraActive) {
                                CameraPreview(
                                    cameraProvider = cameraProvider,
                                    lifecycleOwner = lifecycleOwner,
                                    cameraExecutor = cameraExecutor,
                                    onCodeScanned = { code ->
                                        onCodeScanned(code)
                                        isCameraActive = false
                                        cameraProvider?.unbindAll()
                                        onDismiss()
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Initializing camera...",
                                        style = Typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Long-press the list to share on the second device, and select the ")
                        withStyle(style = SpanStyle(color = Accent)) {
                            append("Share list")
                        }
                        append(" option")
                    },
                    textAlign = TextAlign.Center,
                    color = TextPrimary,
                )

                Spacer(modifier = Modifier.height(60.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StyledButton(
                        onClick = {
                            isCameraActive = false
                            cameraProvider?.unbindAll()
                            onDismiss()
                        },
                        accentColor = PanelActive,
                        modifier = Modifier.width(configuration.screenWidthDp.dp.div(1f / 0.7f)),
                    ) {
                        Text("Close")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun CameraPreview(
    cameraProvider: ProcessCameraProvider?,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: ExecutorService,
    onCodeScanned: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val previewView = PreviewView(context)

            cameraProvider?.let { provider ->
                // Unbind any previous use cases first
                provider.unbindAll()

                // Create use cases
                val preview = Preview.Builder().build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    BarcodeAnalyzer { code ->
                        onCodeScanned(code)
                    }
                )

                preview.surfaceProvider = previewView.surfaceProvider

                try {
                    // Bind use cases to camera
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("ScanShareCodeSheet", "Camera bind error: ${e.localizedMessage}", e)
                }
            }

            previewView
        }
    )
}

class BarcodeAnalyzer(private val scanCallback: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                barcode?.takeIf { it.isNotEmpty() }
                    ?.mapNotNull { it.rawValue }
                    ?.joinToString(",")
                    ?.let { scanCallback(it) }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}