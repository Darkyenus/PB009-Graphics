package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.darkyen.pb009.PointDirection
import com.darkyen.pb009.RasterizationCanvas
import java.lang.Math.*

/**
 *
 */

class LineRasterization : RasterizationCanvas<LineRasterization.Variant>(Variant.values()) {

    val firstHandle = newHandle(-5f, -5f, Color.RED, PointDirection.PointUpRight)
    val secondHandle = newHandle(5f, 5f, Color.GREEN, PointDirection.PointDownLeft)

    override fun drawRaster(variant: Variant) {
        val x0 = firstHandle.pixelX()
        val y0 = firstHandle.pixelY()
        val x1 = secondHandle.pixelX()
        val y1 = secondHandle.pixelY()

        when (variant) {
            Variant.Naive ->
                    naive(x0, y0, x1, y1)
            Variant.DDA ->
                    dda(x0, y0, x1, y1)
            Variant.Bresenham ->
                    bresenham(x0, y0, x1, y1)
            Variant.TracerDDA ->
                    tracerDDA(firstHandle.canvasX(), firstHandle.canvasY(), secondHandle.canvasX(), secondHandle.canvasY())
        }
    }

    fun naive(x1:Int, y1:Int, x2:Int, y2:Int) {
        if (abs(x1 - x2) > abs(y1 - y2)) {
            naiveX(x1, y1, x2, y2)
        } else {
            naiveY(x1, y1, x2, y2)
        }
    }

    fun naiveX(x1:Int, y1:Int, x2:Int, y2:Int) {
        val dy:Float = (y2 - y1).toFloat() / (x2 - x1)
        pixel(x1, y1, Color.GOLD)
        step()

        val dx = x2 - x1
        val range = if (dx >= 0) 1..dx else dx..-1

        for (x in range) {
            val y:Int = Math.round(dy * x)
            pixel(x1 + x, y1 + y, hsv((x - 1f) / (x2 - x1)))
            step()
        }
    }

    fun naiveY(x1:Int, y1:Int, x2:Int, y2:Int) {
        val dx:Float = (x2 - x1).toFloat() / (y2 - y1)
        pixel(x1, y1, Color.WHITE)
        step()

        val dy = y2 - y1
        val range = if (dy >= 0) 1..dy else dy..-1

        for (y in range) {
            val x:Int = Math.round(dx * y)
            pixel(x1 + x, y1 + y, hsv((y - 1f) / (y2 - y1), saturation = 0.5f))
            step()
        }
    }

    fun dda(xp:Int, yp:Int, xk:Int, yk:Int) {
        val deltaX:Float = (xk - xp).toFloat()
        val deltaY:Float = (yk - yp).toFloat()

        val dx:Float
        val dy:Float

        val signX = if (xp < xk) 1f else -1f
        val signY = if (yp < yk) 1f else -1f

        if (abs(deltaX) > abs(deltaY)) {
            // X is more important
            dx = signX
            dy = Math.copySign(deltaY / deltaX, signY)
        } else {
            // Y is more important
            dx = Math.copySign(deltaX / deltaY, signX)
            dy = signY
        }

        var x:Float = xp.toFloat()
        var y:Float = yp.toFloat()

        while (true) {
            val xi:Int = Math.round(x)
            val yi:Int = Math.round(y)
            pixel(xi, yi, rgb(abs(x posMod 1f), abs(y posMod 1f), 0.5f))
            step()

            if (xi == xk && yi == yk) break

            x += dx
            y += dy
        }
    }

    fun bresenham(x1:Int, y1:Int, x2:Int, y2:Int) {
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val signX = signum(x2 - x1)
        val signY = signum(y2 - y1)

        var x = x1
        var y = y1
        var e = 0

        val eAcc:Int
        val eCor:Int
        val eMax:Int
        val corX:Int
        val corY:Int
        val stepX:Int
        val stepY:Int

        if (abs(dx) >= abs(dy)) {
            // X is stepped always
            eAcc = 2 * dy
            eCor = 2 * dx
            eMax = dx

            corX = 0
            corY = signY
            stepX = signX
            stepY = 0
        } else {
            // Y is stepped always
            eAcc = 2 * dx
            eCor = 2 * dy
            eMax = dy

            corX = signX
            corY = 0
            stepX = 0
            stepY = signY
        }

        while (true) {
            var didCorrect = false

            if (e >= eMax) {
                e -= eCor
                x += corX
                y += corY

                didCorrect = true
            }

            pixel(x, y, hsv(((e.toFloat() / eMax) + 1f) / 2f, if (didCorrect) 1f else 0.6f))
            step()

            e += eAcc
            if (x == x2 && y == y2) return
            x += stepX
            y += stepY
        }
    }

    fun tracerDDA(xo:Float, yo:Float, xd:Float, yd:Float) {
        line(0f, -100f, 0f, 100f, color = hsv(0f))
        line(-100f, 0f, 100f, 0f, color = hsv(0f))
        step()

        fun d2c(currentPos:Float, ray:Float):Float {
            if (ray >= 0f) {
                // Ray is positive
                return floor(currentPos + 1f) - currentPos
            } else {
                // Ray is negative
                return currentPos - ceil(currentPos - 1f)
            }
        }

        fun deltaToCross(pos:Vector2, ray:Vector2):Vector2 {
            return Vector2(
                    d2c(pos.x, ray.x),
                    d2c(pos.y, ray.y)
            )
        }

        fun toPixelPos(p:Float, ray:Float):Int {
            val whole = floor(p)
            if (MathUtils.isEqual(whole.toFloat(), p) && ray < 0f) {
                return whole - 1
            } else {
                return whole
            }
        }

        val position = Vector2(xo, yo)
        val ray = Vector2(xd - xo, yd - yo).nor()
        val absRay = Vector2(Math.abs(ray.x), Math.abs(ray.y))

        var totalT = 0f

        val remainingToCross = deltaToCross(position, ray)
        var pixelX = toPixelPos(position.x, ray.x)
        var pixelY = toPixelPos(position.y, ray.y)

        val rayDirectionX:Int = signum(ray.x).toInt()
        val rayDirectionY:Int = signum(ray.y).toInt()

        for (i in 0..127) {
            val tX = remainingToCross.x / absRay.x
            val tY = remainingToCross.y / absRay.y
            val t:Float

            if (tX < tY) {
                // Crossing X first
                t = tX
                pixelX += rayDirectionX
                remainingToCross.x = 1f
                remainingToCross.y -= t * absRay.y
            } else {
                // Crossing Y first
                t = tY
                pixelY += rayDirectionY
                remainingToCross.x -= t * absRay.x
                remainingToCross.y = 1f
            }
            totalT += t
            //position.mulAdd(ray, t)

            val u = if (ray.x > 0f) 1f - remainingToCross.x else remainingToCross.x
            val v = if (ray.y > 0f) 1f - remainingToCross.y else remainingToCross.y

            pixel(pixelX, pixelY, color = rgb(u,v,1f))
            step()
        }

        line(xo, yo, xd, yd, color = hsv(0.2f))
        step()
    }

    enum class Variant {
        Bresenham,
        DDA,
        Naive,
        TracerDDA;

        override fun toString(): String {
            return name.replace('_', ' ')
        }
    }
}