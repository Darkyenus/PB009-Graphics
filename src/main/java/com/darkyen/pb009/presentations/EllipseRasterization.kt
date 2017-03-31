package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas
import java.lang.Math.abs

/**
 *
 */
class EllipseRasterization : RasterizationCanvas<Void?>(arrayOfNulls(1)) {

    val centerHandle = newHandle(0f, 0f, Color.RED, PointDirection.PointUpRight)
    val handle1 = newHandle(0f, 10f, Color.BLUE, PointDirection.PointUpLeft)
    val handle2 = newHandle(10f, 0f, Color.GREEN, PointDirection.PointDownLeft)

    override fun drawRaster(variation: Void?) {
        val centerX = centerHandle.canvasPixelX()
        val centerY = centerHandle.canvasPixelY()
        val x1 = handle1.canvasPixelX() - centerX
        val y1 = handle1.canvasPixelY() - centerY
        val x2 = handle2.canvasPixelX() - centerX
        val y2 = handle2.canvasPixelY() - centerY

        var wrongDefinition = false

        val a:Int
        val aF:Float //Higher precision float of A
        val aDividend:Int = y1*y1 * x2*x2 - y2*y2 * x1*x1
        val aDivisor:Int = y1*y1 - y2*y2
        if (aDivisor == 0) {
            a = 5 //Random sane value
            aF = a.toFloat()
            wrongDefinition = true
        } else {
            aF = sqrt(abs(aDividend.toFloat() / aDivisor))
            a = round(aF)
        }

        val b:Int
        val bDividend:Float = aF*aF * y1*y1
        val bDivisor:Float = aF*aF - x1*x1
        if (MathUtils.isZero(bDivisor)) {
            b = a //Random sane value
            wrongDefinition = true
        } else {
            b = round(sqrt(abs(bDividend / bDivisor)))
        }


        val draw4: Draw4 = { x:Int, y:Int, requestedColor:Float ->
            val color:Float = if (wrongDefinition) Color.RED.toFloatBits() else requestedColor
            val secondaryColor = withAlpha(color, 0.2f)

            pixel(centerX + x, centerY + y, color)
            pixel(centerX + x, centerY - y, secondaryColor)

            pixel(centerX - x, centerY + y, secondaryColor)
            pixel(centerX - x, centerY - y, secondaryColor)

            step()
        }

        bresenham(a, b, draw4)
    }

    fun bresenham(a:Int, b:Int, draw: Draw4) {
        var x = 0
        var y = b
        var d = b*b - a*a*b + a*a/4

        draw(x, y, Color.WHITE.toFloatBits())

        // Area 1
        while (a*a*y > b*b*x) {
            val step:Boolean

            if (d < 0) {
                // Right
                d += b*b*(2*x + 3)

                x += 1
                step = false
            } else {
                // Right & Down
                d += b*b*(2*x + 3) + a*a*(-2*y + 2)

                x += 1
                y -= 1
                step = true
            }

            draw(x, y, hsv(0.1f, if (step) 1f else 0.6f))
        }

        // Area 2

        while (y > 0) {
            val step:Boolean

            if (d < 0) {
                // Down & Right
                d += b*b*(2*x + 2) + a*a*(-2*y + 3)

                y -= 1
                x += 1
                step = true
            } else {
                // Down
                d += a*a*(-2*y + 3)

                y -= 1
                step = false
            }
            draw(x, y, hsv(0.4f, if (step) 1f else 0.6f))
        }
    }
}