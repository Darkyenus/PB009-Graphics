package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Implements the Cohen-Sutherland segment cutting algorithm with halving edge-point resolution
 */
class RectangleSegmentCutting : RasterizationCanvas<RectangleSegmentCutting.Variant>(Variant.values()) {

    companion object {
        val XMax:Byte = 1
        val XMin:Byte = 2
        val YMax:Byte = 4
        val YMin:Byte = 8
    }

    val frameTL = newHandle(-10f, 10f, Color.BROWN, PointDirection.PointDownRight)
    val frameBR = newHandle(10f, -10f, Color.BROWN, PointDirection.PointUpLeft)

    val segment1 = newHandle(0f, 0f, Color.WHITE, PointDirection.PointUpRight)
    val segment2 = newHandle(15f, 0f, Color.WHITE, PointDirection.PointDownLeft)

    fun bitMask(window: Rectangle, x:Float, y:Float):Byte {
        var result:Byte = 0
        if (x > window.x + window.width) {
            result = result or XMax
        } else if (x < window.x) {
            result = result or XMin
        }

        if (y > window.y + window.height) {
            result = result or YMax
        } else if (y < window.y) {
            result = result or YMin
        }
        return result
    }

    fun bitMask(window:Rectangle, handle: Handle):Byte {
        val x = handle.canvasX()
        val y = handle.canvasY()
        val color = Color(0f, 0.7f, 0f, 1f)

        var result:Byte = 0

        if (x > window.x + window.width) {
            result = result or XMax
            color.r = 1f
        } else if (x < window.x) {
            result = result or XMin
            color.r = 0f
        } else {
            color.r = 0.5f
        }

        if (y > window.y + window.height) {
            result = result or YMax
            color.b = 1f
        } else if (y < window.y) {
            result = result or YMin
            color.b = 0f
        } else {
            color.b = 0.5f
        }
        handle.color = color

        return result
    }

    override fun drawRaster(variant: Variant) {
        val clipRect = Rectangle(frameTL.canvasX(), frameTL.canvasY(), 0f, 0f)
        clipRect.merge(frameBR.canvasX(), frameBR.canvasY())

        line(clipRect.x, clipRect.y, clipRect.x + clipRect.width, clipRect.y, color = Color.BROWN.toFloatBits())
        line(clipRect.x + clipRect.width, clipRect.y, clipRect.x + clipRect.width, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())
        line(clipRect.x + clipRect.width, clipRect.y + clipRect.height, clipRect.x, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())
        line(clipRect.x, clipRect.y, clipRect.x, clipRect.y + clipRect.height, color = Color.BROWN.toFloatBits())

        val mask1 = bitMask(clipRect, segment1)
        val mask2 = bitMask(clipRect, segment2)

        if (mask1 or mask2 == 0.toByte()) {
            // Trivial accept
            line(segment1.canvasX(), segment1.canvasY(), segment2.canvasX(), segment2.canvasY(), color = Color.GREEN.toFloatBits())
            step()
            return
        } else if (mask1 and mask2 != 0.toByte()) {
            // Trivial reject
            line(segment1.canvasX(), segment1.canvasY(), segment2.canvasX(), segment2.canvasY(), color = Color.RED.toFloatBits())
            step()
            return
        }

        // Non-trivial
        line(segment1.canvasX(), segment1.canvasY(), segment2.canvasX(), segment2.canvasY(), color = Color(1f, 1f, 0f, 0.15f).toFloatBits())
        step()


        fun drawMid(x:Float, y:Float, color:Float) {
            if (variant == Variant.Without_Midpoints) return
            val overhang = Vector2(segment1.canvasX(), segment1.canvasY()).sub(segment2.canvasX(), segment2.canvasY()).nor().rotate90(1).scl(2f)
            line(x+overhang.x, y+overhang.y, x, y, w1 = 1.5f, w2 = 0.2f, color = color)
            line(x-overhang.x, y-overhang.y, x, y, w1 = 1.5f, w2 = 0.2f, color = color)
            step()
        }

        /**
         * Return the point on p-q segment, which is inside the clipRect and is the nearest one to the point q
         */
        fun clipSecondEndPoint(pIn:Vector2, qIn:Vector2, colorHue:Float, colorValue:Float):Vector2 {
            if (bitMask(clipRect, qIn.x, qIn.y) == 0.toByte()) {
                // Q is already inside, nothing to do here
                return qIn
            } else if (bitMask(clipRect, pIn.x, pIn.y) and bitMask(clipRect, qIn.x, qIn.y) != 0.toByte()) {
                // Q,P segment is completely outside, nothing to do here, just return pIn because that means reject
                return pIn
            } else {
                var pX = pIn.x
                var pY = pIn.y
                var qX = qIn.x
                var qY = qIn.y

                var saturation = 1f
                do {
                    val midX = (pX + qX) / 2f
                    val midY = (pY + qY) / 2f
                    drawMid(midX, midY, hsv(colorHue, saturation, colorValue))
                    saturation *= 0.8f

                    if ((bitMask(clipRect, midX, midY) and bitMask(clipRect, qX, qY)) != 0.toByte()) {
                        // Mid,Q is outside
                        if ((bitMask(clipRect, pX, pY) and bitMask(clipRect, midX, midY)) != 0.toByte()) {
                            // P,Mid is outside - both outside, impossible
                            return pIn
                        } else {
                            // P,Mid is inside
                            qX = midX
                            qY = midY
                        }
                    } else {
                        // Mid,Q is inside
                        pX = midX
                        pY = midY
                    }
                } while (Vector2.dst(pX, pY, qX, qY) > 0.1f)

                return Vector2(qX, qY)
            }
        }

        val p = Vector2(segment1.canvasX(), segment1.canvasY())
        val q = Vector2(segment2.canvasX(), segment2.canvasY())

        val qc = clipSecondEndPoint(p, q, 0f, 1f)
        if (qc.epsilonEquals(p, MathUtils.FLOAT_ROUNDING_ERROR)) {
            //Reject
            line(segment1.canvasX(), segment1.canvasY(), segment2.canvasX(), segment2.canvasY(), color = Color.MAGENTA.toFloatBits())
            step()
        } else {
            val pc = clipSecondEndPoint(q, p, 0.2f, 0.5f)
            // Accept
            line(pc.x, pc.y, qc.x, qc.y, w1 = 2f, w2 = 1f, color = Color.LIME.toFloatBits())
            step()
        }
    }

    enum class Variant {
        With_Midpoints,
        Without_Midpoints
    }
}