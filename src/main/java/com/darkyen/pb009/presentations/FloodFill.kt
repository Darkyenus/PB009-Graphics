package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.LongArray as LongList
import com.darkyen.pb009.RasterizationCanvas

/**
 *
 */
class FloodFill : RasterizationCanvas<FloodFill.Variant>(Variant.values()) {

    val framePointCount = 6
    val framePoints = Array(framePointCount) { index ->
        val angle = (index.toFloat() / framePointCount) * MathUtils.PI2
        val x = MathUtils.sin(angle) * 13f
        val y = MathUtils.cos(angle) * 13f
        newHandle(x, y, Color.LIGHT_GRAY)
    }

    var xMin = 0
    var xMax = 0
    var yMin = 0
    var yMax = 0
    var filledField:BooleanArray = kotlin.BooleanArray(0)
    var filledFieldLineLength = 0

    fun isFilled(pointX:Int, pointY:Int):Boolean {
        if (pointX < xMin || pointX > xMax || pointY < yMin || pointY > yMax) return true

        return filledField[pointX-xMin + (pointY-yMin) * filledFieldLineLength]
    }

    fun fill(pointX: Int, pointY: Int) {
        if (pointX < xMin || pointX > xMax || pointY < yMin || pointY > yMax) return

        filledField[pointX-xMin + (pointY-yMin) * filledFieldLineLength] = true
    }

    val fillBeginHandle = newHandle(0f, 0f, Color.NAVY)

    override fun drawRaster(variant: Variant) {
        xMin = framePoints[0].pixelX()
        xMax = xMin
        yMin = framePoints[0].pixelY()
        yMax = yMin
        for (framePoint in framePoints) {
            xMin = minOf(xMin, framePoint.pixelX())
            xMax = maxOf(xMax, framePoint.pixelX())
            yMin = minOf(yMin, framePoint.pixelY())
            yMax = maxOf(yMax, framePoint.pixelY())
        }
        val overlap = 5
        xMin -= overlap
        xMax += overlap
        yMin -= overlap
        yMax += overlap

        filledFieldLineLength = (xMax - xMin + 1)
        filledField = kotlin.BooleanArray(filledFieldLineLength * (yMax - yMin + 1))

        val borderPolygonColor = Color.LIGHT_GRAY.toFloatBits()
        for (i in 0..(framePointCount-1)) {
            val first = framePoints[i]
            xMin = minOf(xMin, first.pixelX())
            xMax = maxOf(xMax, first.pixelX())
            yMin = minOf(yMin, first.pixelY())
            yMax = maxOf(yMax, first.pixelY())

            val second = framePoints[(i+1) % framePointCount]
            bresenham(first.pixelX(), first.pixelY(),
                    second.pixelX(), second.pixelY()) { x, y ->
                pixel(x,y, borderPolygonColor)
                filledField[x-xMin + (y-yMin) * filledFieldLineLength] = true
            }
        }

        val fillBeginX = fillBeginHandle.pixelX()
        val fillBeginY = fillBeginHandle.pixelY()
        val validStartPoint = !isFilled(fillBeginX, fillBeginY)
        pixel(fillBeginX, fillBeginY, if (validStartPoint) Color.NAVY else Color.RED)
        step()

        if (!validStartPoint) return

        when (variant) {
            Variant.FloodFill_DepthFirst_Ordered -> {
                floodFill(fillBeginX, fillBeginY, true, true)
            }
            Variant.FloodFill_DepthFirst_Unordered -> {
                floodFill(fillBeginX, fillBeginY, false, true)
            }
            Variant.FloodFill_BreadthFirst -> {
                floodFill(fillBeginX, fillBeginY, false, false)
            }
            Variant.LineSeedFill -> {
                lineSeedFill(fillBeginX, fillBeginY)
            }
        }
    }

    fun key(x:Int, y:Int):Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFF_FFFFL)
    operator fun Long.component1():Int = (this ushr 32).toInt()
    operator fun Long.component2():Int = (this and 0xFFFF_FFFFL).toInt()

    fun floodFill(beginX:Int, beginY:Int, ordered:Boolean, depthFirst:Boolean) {
        val totalPixels = filledField.size
        val coordStack = LongList(ordered, totalPixels)

        fun add(x:Int, y:Int) {
            if (isFilled(x, y)) return
            val key = key(x, y)
            if (!coordStack.contains(key)) {
                coordStack.add(key)
            }
        }

        coordStack.add(key(beginX, beginY))
        var remaining = totalPixels //Should not be needed, just in case, and used for color

        while (remaining > 0 && coordStack.size > 0) {
            val (x, y) = if(depthFirst) coordStack.removeIndex(0) else coordStack.pop()

            if (!isFilled(x, y)) {
                remaining -= 1
                pixel(x, y, hsv(remaining.toFloat() / totalPixels))
                fill(x, y)
                step()

                add(x-1, y)
                add(x+1, y)
                add(x, y+1)
                add(x, y-1)
            }
        }
    }

    fun lineSeedFill(beginX: Int, beginY: Int) {
        val totalPixels = filledField.size
        var remaining = totalPixels
        val seedStack = LongList(false, totalPixels)

        fun seed(x:Int, y:Int) {
            seedStack.add(key(x, y))
            pixel(x, y, hsv(0.3f, 0.3f, 1f))
            step()
        }

        fun mark(x:Int, y:Int) {
            pixel(x, y, hsv(remaining.toFloat() / totalPixels, alpha = 0.5f))
            fill(x, y)
            remaining -= 1
        }

        fun traverseLine(fromX:Int, y:Int, upSeededInitially:Boolean, downSeededInitially:Boolean, xDirection:Int) {
            var x = fromX + xDirection
            var upSeeded = upSeededInitially
            var downSeeded = downSeededInitially
            while (!isFilled(x, y)) {
                mark(x, y)
                step()

                if (isFilled(x, y + 1)) {
                    //Up is filled, it will need seeding later
                    upSeeded = false
                } else if (!upSeeded) {
                    //Up is not seeded and we CAN seed, do it
                    seed(x, y + 1)
                    upSeeded = true
                }

                if (isFilled(x, y - 1)) {
                    //Down is filled, it will need seeding later
                    downSeeded = false
                } else if (!downSeeded) {
                    //Up is not seeded and we CAN seed, do it
                    seed(x, y - 1)
                    downSeeded = true
                }

                x += xDirection
            }
        }

        seed(beginX, beginY)

        while (remaining > 0 && seedStack.size > 0) {
            val (x, y) = seedStack.pop()
            if (isFilled(x, y)) continue
            mark(x, y)
            step()

            val upSeeded = if (!isFilled(x, y+1)) {
                //Seed up
                seed(x, y+1)
                true
            } else false
            val downSeeded = if (!isFilled(x, y-1)) {
                //Seed down
                seed(x, y-1)
                true
            } else false

            traverseLine(x, y, upSeeded, downSeeded, -1)
            traverseLine(x, y, upSeeded, downSeeded, 1)
        }
    }

    enum class Variant {
        FloodFill_DepthFirst_Ordered,
        FloodFill_DepthFirst_Unordered,
        FloodFill_BreadthFirst,
        LineSeedFill
    }

    /**
     * Modified bresenham with custom handler
     */
    fun bresenham(x1:Int, y1:Int, x2:Int, y2:Int, handler:(x:Int, y:Int) -> Unit) {
        val dx = Math.abs(x2 - x1)
        val dy = Math.abs(y2 - y1)
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

        if (Math.abs(dx) >= Math.abs(dy)) {
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
            if (e >= eMax) {
                e -= eCor
                x += corX
                y += corY
            }

            handler(x, y)

            e += eAcc
            if (x == x2 && y == y2) return
            x += stepX
            y += stepY
        }
    }
}