package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas
import com.badlogic.gdx.utils.Array as OArray

/**
 *
 */
class PolygonClipping : RasterizationCanvas<PolygonClipping.Variant>(Variant.values()) {

    val frameTL = newHandle(-10f, 10f, Color.BROWN, PointDirection.PointDownRight)
    val frameBR = newHandle(10f, -10f, Color.BROWN, PointDirection.PointUpLeft)

    val polygonPointCount = 5
    val polygonPoints = Array(polygonPointCount) { index ->
        val angle = (index.toFloat() / polygonPointCount) * MathUtils.PI2
        val x = MathUtils.sin(angle) * 13f
        val y = MathUtils.cos(angle) * 13f
        newHandle(x, y, Color.CHARTREUSE)
    }


    override fun drawRaster(variant: Variant) {
        val clipRect = Rectangle(frameTL.canvasX(), frameTL.canvasY(), 0f, 0f)
        clipRect.merge(frameBR.canvasX(), frameBR.canvasY())

        line(clipRect.x, clipRect.y, clipRect.x + clipRect.width, clipRect.y, color = Color.BROWN.toFloatBits())
        line(clipRect.x + clipRect.width, clipRect.y, clipRect.x + clipRect.width, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())
        line(clipRect.x + clipRect.width, clipRect.y + clipRect.height, clipRect.x, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())
        line(clipRect.x, clipRect.y, clipRect.x, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())

        val polygon = newPolygon()
        for (polygonPointHandle in polygonPoints) {
            polygon.add(Point(polygonPointHandle.canvasX(), polygonPointHandle.canvasY()))
        }

        drawPolygon(polygon, Color.LIGHT_GRAY.cpy().apply { a = 0.3f }.toFloatBits())
        step()

        when (variant) {
            Variant.XMax -> {
                val newPolygon = clip(clipRect.x + clipRect.width, polygon)
                drawPolygon(newPolygon, Color.CHARTREUSE.toFloatBits())
                step()
            }
            Variant.Full -> {
                val xMaxFlip = flip(clip(clipRect.x + clipRect.width, polygon))
                val yMaxFlip = flip(clip(clipRect.y + clipRect.height, xMaxFlip))
                val xMinFlip = flip(clip(-clipRect.x, yMaxFlip))
                val yMinFlip = flip(clip(-clipRect.y, xMinFlip))

                drawPolygon(yMinFlip, Color.CHARTREUSE.toFloatBits())
                step()
            }
        }

    }

    fun drawPolygon(polygon:OArray<Point>, color:Float) {
        for (i in 0..(polygon.size-1)) {
            val first = polygon[i]
            val second = polygon[(i+1) % polygon.size]
            line(first.x, first.y, second.x, second.y, color = color)
        }
    }

    fun newPolygon():OArray<Point> = OArray(true, 32)

    fun flip(polygon:OArray<Point>):OArray<Point> {
        val result = newPolygon()
        for (point in polygon) {
            result.add(Point(point.y, -point.x))
        }
        return result
    }

    fun clip(xMax:Float, polygon: OArray<Point>):OArray<Point> {
        val result = newPolygon()
        for (i in 0..(polygon.size-1)) {
            val current = polygon[i]
            val next = polygon[(i+1) % polygon.size]

            val currentInside = current.x < xMax
            val nextInside = next.x < xMax

            if (currentInside && nextInside) {
                // Both inside, just emit point
                result.add(current)
            } else if (currentInside && !nextInside) {
                // Now inside, but going out - emit this point and clip point
                result.add(current)
                result.add(Point(xMax, MathUtils.lerp(current.y, next.y, (xMax - current.x)/(next.x - current.x))))
            } else if (!currentInside && nextInside) {
                // Now outside, going inside, emit just clip point
                result.add(Point(xMax, MathUtils.lerp(current.y, next.y, (xMax - current.x)/(next.x - current.x))))
            } // else Both outside, do nothing
        }

        return result
    }

    class Point(val x:Float, val y:Float)

    enum class Variant {
        XMax,
        Full
    }
}