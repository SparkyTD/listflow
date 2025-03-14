package com.firestormsw.listflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.ui.theme.TextSecondary

@Composable
fun SheetDragHandle() {
    Column(
        modifier = Modifier
            .width(50.dp)
            .height(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .width(32.dp)
                .height(6.dp)
                .padding(vertical = 8.dp)
        ) {}

        val dragHandleColor = TextSecondary

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            drawRoundRect(
                color = dragHandleColor,
                cornerRadius = CornerRadius(16.0f, 16.0f)
            )
        }
    }
}