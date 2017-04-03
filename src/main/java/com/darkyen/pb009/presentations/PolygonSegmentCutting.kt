package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas

/**
 *
 */
class PolygonSegmentCutting : RasterizationCanvas<PolygonSegmentCutting.Variant>(Variant.values()) {

    val frameTL = newHandle(-5f, 5f, Color.FIREBRICK, PointDirection.PointDownRight)
    val frameTR = newHandle(5f, 5f, Color.FIREBRICK, PointDirection.PointDownLeft)
    val frameBR = newHandle(5f, -5f, Color.FIREBRICK, PointDirection.PointUpLeft)
    val frameBL = newHandle(-5f, -5f, Color.FIREBRICK, PointDirection.PointUpRight)

    val segment1 = newHandle(-5f, 10f, Color.FOREST, PointDirection.PointDownRight)
    val segment2 = newHandle(5f, -10f, Color.FOREST, PointDirection.PointUpLeft)

    override fun drawRaster(variation: Variant) {
        val lines = arrayOf(
                Line(frameTL, frameTR),
                Line(frameTR, frameBR),
                Line(frameBR, frameBL),
                Line(frameBL, frameTL)
        )
        // Frame
        for (line in lines) {
            line(line.x1, line.y1, line.x2, line.y2, color = Color.FIREBRICK.toFloatBits())
            if (variation == Variant.With_Midpoints) {
                val centerX = (line.x1 + line.x2) / 2f
                val centerY = (line.y1 + line.y2) / 2f
                line(centerX, centerY, centerX + line.normal.x, centerY + line.normal.y, 1.2f, 0.2f, Color.SKY.toFloatBits())
            }
        }
        step()

        // Line skeleton
        line(segment1.canvasX(), segment1.canvasY(), segment2.canvasX(), segment2.canvasY(), color = Color(0f, 4f, 0f, 0.15f).toFloatBits())
        step()

        val A = Vector2(segment1.canvasX(), segment1.canvasY())
        val BA = Vector2(segment2.canvasX(), segment2.canvasY()).sub(A)

        var tMin = 0f
        var tMax = 1f
        for (line in lines) {
            if (tMin >= tMax) break

            val pe = Vector2(line.x1, line.y1)
            val normal = line.normal
            val dot = normal.dot(BA)

            if (MathUtils.isZero(dot)) {
                if (normal.dot(A.x - pe.x, A.y - pe.y) >= 0) {
                    // Completely out of frame, reject
                    tMin = 1f //For nice debug draw
                    tMax = 0f
                    break
                }
            } else {
                val t0 = (-normal.dot(A.x - pe.x, A.y - pe.y)) / (normal.dot(BA))
                if (dot < 0f) {
                    tMin = maxOf(tMin, t0)
                } else {
                    tMax = minOf(tMax, t0)
                }

                if (variation == Variant.With_Midpoints) {
                    // Debug draw
                    val t0X = MathUtils.lerp(segment1.canvasX(), segment2.canvasX(), t0)
                    val t0Y = MathUtils.lerp(segment1.canvasY(), segment2.canvasY(), t0)
                    val t0N = BA.cpy().nor().rotate90(1)

                    line(t0X - t0N.x, t0Y - t0N.y, t0X + t0N.x, t0Y + t0N.y, color = Color.GRAY.toFloatBits())
                }
            }
        }

        val tMinX = MathUtils.lerp(segment1.canvasX(), segment2.canvasX(), tMin)
        val tMinY = MathUtils.lerp(segment1.canvasY(), segment2.canvasY(), tMin)
        val tMaxX = MathUtils.lerp(segment1.canvasX(), segment2.canvasX(), tMax)
        val tMaxY = MathUtils.lerp(segment1.canvasY(), segment2.canvasY(), tMax)
        if (tMin < tMax) {
            // Success
            line(tMinX, tMinY, tMaxX, tMaxY, color = Color.GREEN.toFloatBits())
            step()
        } else {
            // Rejected
            if (variation == Variant.With_Midpoints) {
                line(tMinX, tMinY, tMaxX, tMaxY, color = Color.RED.toFloatBits())
                step()
            }
        }
    }

    private class Line(h1:Handle, h2:Handle) {
        val x1 = h1.canvasX()
        val y1 = h1.canvasY()

        val x2 = h2.canvasX()
        val y2 = h2.canvasY()

        val normal:Vector2 = Vector2(x1, y1).sub(x2, y2).rotate90(-1).nor()
    }

    enum class Variant {
        With_Midpoints,
        Without_Midpoints
    }
}