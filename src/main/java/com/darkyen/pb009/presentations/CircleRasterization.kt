package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas
import java.lang.Math.abs

/**
 *
 */
class CircleRasterization : RasterizationCanvas<CircleRasterization.CircleType>(CircleType.values()) {

    val centerHandle = newHandle(0f, 0f, Color.RED)
    val radiusHandle = newHandle(10f, 10f, Color.GREEN, PointDirection.PointDownLeft)

    override fun drawRaster(variation: CircleType) {
        val centerX = centerHandle.canvasPixelX()
        val centerY = centerHandle.canvasPixelY()
        val radiusX = radiusHandle.canvasPixelX()
        val radiusY = radiusHandle.canvasPixelY()
        val radius = Math.round(Vector2.dst(centerX.toFloat(), centerY.toFloat(), radiusX.toFloat(), radiusY.toFloat()))

        val draw8: Draw8 = { x:Int, y:Int, color:Float ->
            val secondaryColor = withAlpha(color, 0.2f)

            pixel(centerX + x, centerY + y, color)
            pixel(centerX + x, centerY - y, secondaryColor)

            pixel(centerX - x, centerY + y, secondaryColor)
            pixel(centerX - x, centerY - y, secondaryColor)

            pixel(centerX + y, centerY + x, secondaryColor)
            pixel(centerX + y, centerY - x, secondaryColor)

            pixel(centerX - y, centerY + x, secondaryColor)
            pixel(centerX - y, centerY - x, secondaryColor)

            step()
        }

        when (variation) {
            CircleType.Naive -> naive(radius, draw8)
            CircleType.Decision_Member -> decisionMember(radius, draw8)
            CircleType.Bresenham -> bresenham(radius, draw8)
        }
    }

    fun naive(radius:Int, draw: Draw8) {
        draw(0, radius, Color.WHITE.toFloatBits())
        for (x:Int in 1..ceil(radius / sqrt(2f))) {
            val y:Float = sqrt(radius * radius - x*x)

            draw(x, round(y), hsv(y / radius, y % 1))
        }
    }

    fun decisionMember(radius:Int, draw: Draw8) {
        draw(0, radius, Color.WHITE.toFloatBits())
        var y = radius
        for (x:Int in 1..ceil(radius / sqrt(2f))) {
            val d:Float = po2(x) + po2(y - 0.5f) - po2(radius)

            val color:Float = hsv(abs(d) / radius, if (d >= 0) 1f else 0.5f)

            if (d >= 0) {
                y -= 1
            }

            draw(x, y, color)
        }
    }

    fun bresenham(radius:Int, draw: Draw8) {
        var x = 0
        var y = radius
        var d = 1 - radius

        draw(x, y, Color.WHITE.toFloatBits())

        while (y > x) {
            if (d < 0) {
                d += 2*x + 3
                x += 1

                draw(x, y, Color.GOLD.toFloatBits())
            } else {
                d += 2*x - 2*y + 5
                x += 1
                y -= 1

                draw(x, y, Color.GOLDENROD.toFloatBits())
            }
        }
    }


    enum class CircleType {
        Naive,
        Decision_Member,
        Bresenham
    }
}