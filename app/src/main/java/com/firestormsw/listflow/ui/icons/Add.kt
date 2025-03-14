package com.firestormsw.listflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Add: ImageVector
	get() {
		if (_Add != null) {
			return _Add!!
		}
		_Add = ImageVector.Builder(
            name = "Add",
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
				moveTo(440f, 520f)
				horizontalLineTo(200f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(240f)
				verticalLineToRelative(-240f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(240f)
				horizontalLineToRelative(240f)
				verticalLineToRelative(80f)
				horizontalLineTo(520f)
				verticalLineToRelative(240f)
				horizontalLineToRelative(-80f)
				close()
			}
		}.build()
		return _Add!!
	}

private var _Add: ImageVector? = null
