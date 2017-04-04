package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas

/**
 *
 */
class TriangleFill : RasterizationCanvas<TriangleFill.Variant>(Variant.values()) {

    val triangle0 = newHandle(0f, 10f, Color.GRAY, PointDirection.PointDownRight)
    val triangle1 = newHandle(7f, 0f, Color.GRAY, PointDirection.PointUpLeft)
    val triangle2 = newHandle(-7f, 0f, Color.GRAY, PointDirection.PointUpRight)

    override fun drawRaster(variant: Variant) {
        val xMin = minOf(triangle0.pixelX(), triangle1.pixelX(), triangle2.pixelX()) - 1
        val xMax = maxOf(triangle0.pixelX(), triangle1.pixelX(), triangle2.pixelX()) + 1
        val yMin = minOf(triangle0.pixelY(), triangle1.pixelY(), triangle2.pixelY()) - 1
        val yMax = maxOf(triangle0.pixelY(), triangle1.pixelY(), triangle2.pixelY()) + 1

        val l0 = Line(triangle1, triangle0, triangle2)
        val l1 = Line(triangle2, triangle1, triangle0)
        val l2 = Line(triangle0, triangle2, triangle1)

        when (variant) {
            Variant.Simple -> {
                for (y in yMin..yMax) {
                    for (x in xMin..xMax) {
                        val e0 = l0.e(x, y)
                        val e1 = l1.e(x, y)
                        val e2 = l2.e(x, y)

                        if (e0 >= 0 && e1 >= 0 && e2 >= 0) {
                            pixel(x, y, rgb(
                                    (e0 % l0.distanceToOther) / l0.distanceToOther,
                                    (e1 % l1.distanceToOther) / l1.distanceToOther,
                                    (e2 % l2.distanceToOther) / l2.distanceToOther))
                            step()
                        } else if (e0 <= 0 && e1 <= 0 && e2 <= 0 && (x + y) % 2 == 0) {
                            pixel(x, y, hsv(1f, 0.1f))
                            step()
                        }
                    }
                }
            }
            Variant.Optimized -> {
                for (y in yMin..yMax) {
                    var drawn = false
                    var e0 = l0.e(xMin, y)
                    var e1 = l1.e(xMin, y)
                    var e2 = l2.e(xMin, y)

                    for (x in xMin..xMax) {
                        if (e0 or e1 or e2 >= 0) {
                            pixel(x, y, rgb(
                                    e0/l0.normalizedDistanceToOther,
                                    e1/l1.normalizedDistanceToOther,
                                    e2/l2.normalizedDistanceToOther))
                            step()
                            drawn = true
                        } else if (drawn) {
                            break
                        }

                        e0 += l0.a
                        e1 += l1.a
                        e2 += l2.a
                    }
                }
            }
            Variant.MultiSampled2 -> {
                drawMultiSampled(xMin, xMax, yMin, yMax, l0, l1, l2, 2)
            }
            Variant.MultiSampled4 -> {
                drawMultiSampled(xMin, xMax, yMin, yMax, l0, l1, l2, 4)
            }
            Variant.MultiSampled16 -> {
                drawMultiSampled(xMin, xMax, yMin, yMax, l0, l1, l2, 16)
            }
        }
    }

    private fun drawMultiSampled(xMin:Int, xMax:Int, yMin:Int, yMax:Int, l0:Line, l1:Line, l2:Line, samples:Int) {
        val maxHits = samples * samples
        val sampleStep = 1f / (samples + 1)
        for (y in yMin..yMax) {
            for (x in xMin..xMax) {
                var hits = 0

                for (xSample in 1..samples) {
                    for (ySample in 1..samples) {
                        val fragX = x + sampleStep * xSample
                        val fragY = y + sampleStep * ySample

                        val e0 = l0.e(fragX, fragY)
                        val e1 = l1.e(fragX, fragY)
                        val e2 = l2.e(fragX, fragY)

                        if (e0 >= 0 && e1 >= 0 && e2 >= 0) {
                            hits++
                        }
                    }
                }

                if (hits > 0) {
                    pixel(x, y, hsv(1f, saturation = 0f, value = 1f, alpha = hits.toFloat() / maxHits))
                    step()
                }
            }
        }
    }


    private class Line(handle0:Handle, handle1:Handle, otherHandle:Handle) {
        val a: Int = (handle0.pixelY() - handle1.pixelY())
        val b: Int = (handle1.pixelX() - handle0.pixelX())
        val c: Int = (handle0.pixelX() * handle1.pixelY() - handle1.pixelX() * handle0.pixelY())

        val distanceToOther:Float = Intersector.distanceLinePoint(
                handle0.pixelX().toFloat(), handle0.pixelY().toFloat(),
                handle1.pixelX().toFloat(), handle1.pixelY().toFloat(),
                otherHandle.pixelX().toFloat(), otherHandle.pixelY().toFloat())

        val normalizedDistanceToOther = distanceToOther * Math.sqrt((a*a + b*b).toDouble()).toFloat()

        fun e(x:Int, y:Int) = a * x + b * y + c
        fun e(x:Float, y:Float) = a * x + b * y + c
    }

    enum class Variant {
        Simple,
        Optimized,
        MultiSampled2,
        MultiSampled4,
        MultiSampled16,
    }
}