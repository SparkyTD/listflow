package com.firestormsw.listflow.ui.sheets

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.viewmodel.ListflowViewModel
import com.firestormsw.listflow.ui.components.ListSelectorChip
import com.firestormsw.listflow.ui.components.MeasureViewSize
import com.firestormsw.listflow.ui.components.SheetDragHandle
import com.firestormsw.listflow.ui.components.StyledButton
import com.firestormsw.listflow.ui.icons.Add
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.Background
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary
import com.firestormsw.listflow.ui.theme.Typography
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareListSheet(
    isOpen: Boolean,
    viewModel: ListflowViewModel,
    list: ListModel?,
    onDismiss: () -> Unit
) {
    if (!isOpen || list == null) {
        return
    }

    var qrCodeDrawable: Drawable? by remember { mutableStateOf(null) }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { SheetDragHandle() },
        sheetState = SheetState(
            skipPartiallyExpanded = true,
            density = Density(context)
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
                    text = "Share this list",
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
                    .padding(vertical = 0.dp),
            ) {
                if (qrCodeDrawable != null) {
                    Image(
                        painter = rememberDrawablePainter(qrCodeDrawable),
                        contentDescription = null,
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = Accent,
                            trackColor = TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            val addButton: @Composable () -> Unit = {
                ListSelectorChip(
                    selected = false,
                    text = "Add",
                    leadingIcon = { Icon(Add, contentDescription = null) },
                )
            }
            MeasureViewSize(viewToMeasure = addButton) { measureSize ->
                Text(
                    text = buildAnnotatedString {
                        append("Long-press the ")
                        appendInlineContent("add-btn")
                        append(" button on the second device, and select the ")
                        withStyle(style = SpanStyle(color = Accent)) {
                            append("Scan share code")
                        }
                        append(" option")
                    },
                    textAlign = TextAlign.Center,
                    color = TextPrimary,
                    inlineContent = mapOf(
                        "add-btn" to InlineTextContent(
                            Placeholder(measureSize.width.value.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
                        ) {
                            addButton()
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                StyledButton(
                    onClick = onDismiss,
                    accentColor = PanelActive,
                    modifier = Modifier.width(configuration.screenWidthDp.dp.div(1f / 0.7f)),
                ) {
                    Text("Close", style = Typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        LaunchedEffect(list.id) {
            viewModel.generateShareCode(list) { code ->
                val data = QrData.Text(code)
                val options = createQrVectorOptions {
                    errorCorrectionLevel = QrErrorCorrectionLevel.Low
                    padding = 0f

                    colors {
                        dark = QrVectorColor.Solid(Color(TextPrimary.toArgb().toLong()))
                        ball = QrVectorColor.Solid(Color(Accent.toArgb().toLong()))
                        frame = QrVectorColor.Solid(Color(Accent.toArgb().toLong()))
                    }

                    shapes {
                        darkPixel = QrVectorPixelShape.RoundCorners(.5f)
                        ball = QrVectorBallShape.RoundCorners(.25f)
                        frame = QrVectorFrameShape.RoundCorners(.25f)
                    }
                }

                qrCodeDrawable = QrCodeDrawable(data, options)
            }
        }
    }
}