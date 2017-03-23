package com.darkyen.pb009

import com.badlogic.gdx.graphics.Color
import java.lang.Math.abs

/**
 *
 */

class LineRasterization : RasterizationCanvas<LineRasterization.LineAlgorithm>(LineAlgorithm.values()) {

    val firstHandle = newHandle(Color.RED)
    val secondHandle = newHandle(Color.GREEN)

    override fun drawRaster(variation:LineAlgorithm) {
        val x0 = firstHandle.canvasX()
        val y0 = firstHandle.canvasY()
        val x1 = secondHandle.canvasX()
        val y1 = secondHandle.canvasY()

        when (variation) {
            LineAlgorithm.Naive ->
                    naive(x0, y0, x1, y1)
            LineAlgorithm.DDA ->
                    dda(x0, y0, x1, y1)
            LineAlgorithm.Bresenham ->
                    bresenham(x0, y0, x1, y1)
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

        val dx = x2 - x1
        val range = if (dx >= 0) 1..dx else dx..-1

        for (x in range) {
            val y:Int = Math.round(dy * x)
            pixel(x1 + x, y1 + y, hsv((x - 1f) / (x2 - x1)))
        }
    }

    fun naiveY(x1:Int, y1:Int, x2:Int, y2:Int) {
        val dx:Float = (x2 - x1).toFloat() / (y2 - y1)
        pixel(x1, y1, Color.WHITE)

        val dy = y2 - y1
        val range = if (dy >= 0) 1..dy else dy..-1

        for (y in range) {
            val x:Int = Math.round(dx * y)
            pixel(x1 + x, y1 + y, hsv((y - 1f) / (y2 - y1), saturation = 0.5f))
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

            e += eAcc
            if (x == x2 && y == y2) return
            x += stepX
            y += stepY
        }
    }

    enum class LineAlgorithm {
        Bresenham,
        DDA,
        Naive;

        override fun toString(): String {
            return name.replace('_', ' ')
        }
    }
}