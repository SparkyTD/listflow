package com.firestormsw.listflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val KeyboardArrowDown: ImageVector
    get() {
        if (_Keyboard_arrow_down != null) {
            return _Keyboard_arrow_down!!
        }
        _Keyboard_arrow_down = ImageVector.Builder(
            name = "Keyboard_arrow_down",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(480f, 616f)
                lineTo(240f, 376f)
                lineToRelative(56f, -56f)
                lineToRelative(184f, 184f)
                lineToRelative(184f, -184f)
                lineToRelative(56f, 56f)
                close()
            }
        }.build()
        return _Keyboard_arrow_down!!
    }

private var _Keyboard_arrow_down: ImageVector? = null
