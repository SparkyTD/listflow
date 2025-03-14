package com.firestormsw.listflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Share: ImageVector
    get() {
        if (_Share != null) {
            return _Share!!
        }
        _Share = ImageVector.Builder(
            name = "Share",
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
                moveTo(720f, 880f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                quadToRelative(0f, -7f, 1f, -14.5f)
                reflectiveQuadToRelative(3f, -13.5f)
                lineTo(322f, 568f)
                quadToRelative(-17f, 15f, -38f, 23.5f)
                reflectiveQuadToRelative(-44f, 8.5f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                reflectiveQuadToRelative(35f, -85f)
                reflectiveQuadToRelative(85f, -35f)
                quadToRelative(23f, 0f, 44f, 8.5f)
                reflectiveQuadToRelative(38f, 23.5f)
                lineToRelative(282f, -164f)
                quadToRelative(-2f, -6f, -3f, -13.5f)
                reflectiveQuadToRelative(-1f, -14.5f)
                quadToRelative(0f, -50f, 35f, -85f)
                reflectiveQuadToRelative(85f, -35f)
                reflectiveQuadToRelative(85f, 35f)
                reflectiveQuadToRelative(35f, 85f)
                reflectiveQuadToRelative(-35f, 85f)
                reflectiveQuadToRelative(-85f, 35f)
                quadToRelative(-23f, 0f, -44f, -8.5f)
                reflectiveQuadTo(638f, 288f)
                lineTo(356f, 452f)
                quadToRelative(2f, 6f, 3f, 13.5f)
                reflectiveQuadToRelative(1f, 14.5f)
                reflectiveQuadToRelative(-1f, 14.5f)
                reflectiveQuadToRelative(-3f, 13.5f)
                lineToRelative(282f, 164f)
                quadToRelative(17f, -15f, 38f, -23.5f)
                reflectiveQuadToRelative(44f, -8.5f)
                quadToRelative(50f, 0f, 85f, 35f)
                reflectiveQuadToRelative(35f, 85f)
                reflectiveQuadToRelative(-35f, 85f)
                reflectiveQuadToRelative(-85f, 35f)
                moveToRelative(0f, -640f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(760f, 200f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(720f, 160f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(680f, 200f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(720f, 240f)
                moveTo(240f, 520f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(280f, 480f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(240f, 440f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(200f, 480f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(240f, 520f)
                moveToRelative(480f, 280f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(760f, 760f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(720f, 720f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(680f, 760f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(720f, 800f)
                moveToRelative(0f, -40f)
            }
        }.build()
        return _Share!!
    }

private var _Share: ImageVector? = null
