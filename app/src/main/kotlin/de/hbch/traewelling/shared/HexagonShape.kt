package de.hbch.traewelling.shared

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density

/**
 * Forms a hexagon shape with symmetric sides
 */
open class HexagonShape(val intent: Float = 0.2f, val peek: Float = 0.5f) : Shape {


    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * intent, 0f)
            lineTo(size.width * (1 - intent), 0f)
            lineTo(size.width, size.height * peek)
            lineTo(size.width * (1 - intent), size.height)
            lineTo(size.width * intent, size.height)
            lineTo(0f, size.height * peek)
            close()
        }
        return Outline.Generic(path)
    }
}
