package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

/**
 * Draw function that draws 4 pixels at the same time
 */
typealias Draw4 = (x:Int, y:Int, color:Float) -> Unit
/**
 * Draw function that draws 8 pixels at the same time
 */
typealias Draw8 = (x:Int, y:Int, color:Float) -> Unit

fun signum(n:Int):Int {
    if (n < 0) return -1
    if (n > 0) return 1
    return 0
}

fun ceil(f:Float):Int {
    return Math.ceil(f.toDouble()).toInt()
}

fun round(f:Float):Int {
    return Math.round(f.toDouble()).toInt()
}

fun floor(f:Float):Int {
    return Math.floor(f.toDouble()).toInt()
}

fun sqrt(f:Int):Float {
    return Math.sqrt(f.toDouble()).toFloat()
}

fun pow(base:Float, exponent:Float):Float {
    return Math.pow(base.toDouble(), exponent.toDouble()).toFloat()
}

fun po2(base:Float):Float {
    return base * base
}

fun po2(base:Int):Int {
    return base * base
}

fun sqrt(f:Float):Float {
    return Math.sqrt(f.toDouble()).toFloat()
}

infix fun Float.posMod(f:Float):Float {
    val result = this % f
    if (result < 0) return result + f
    return result
}

private val color_TMP = Color()
/**
 * All parameters are 0-1
 *
 * https://en.wikipedia.org/wiki/HSL_and_HSV#From_HSV
 */
fun hsv(hue:Float, saturation:Float = 1f, value:Float = 1f, alpha:Float = 1f):Float {
    val c = value * saturation
    val h = (if (hue < 0) (hue % 1 + 1) else (hue % 1)) * 6
    val x = c * (1 - Math.abs(h % 2 - 1))

    val r:Float
    val g:Float
    val b:Float

    if (h <= 1) {
        r = c
        g = x
        b = 0f
    } else if (h <= 2) {
        r = x
        g = c
        b = 0f
    } else if (h <= 3) {
        r = 0f
        g = c
        b = x
    } else if (h <= 4) {
        r = 0f
        g = x
        b = c
    } else if (h <= 5) {
        r = x
        g = 0f
        b = c
    } else {
        r = c
        g = 0f
        b = x
    }

    return color_TMP.set(r, g, b, alpha).toFloatBits()
}

fun rgb(r:Float, g:Float, b:Float):Float {
    return color_TMP.set(r, g, b, 1f).toFloatBits()
}

fun withAlpha(color:Float, newAlpha:Float):Float {
    val bits = java.lang.Float.floatToRawIntBits(color) and (255 shl 24).inv() or (((newAlpha * 255).toInt() and 255) shl 24)
    return java.lang.Float.intBitsToFloat(bits)
}

private val createLineVertices_Vertices = FloatArray(5*4)
private val createLineVertices_V1 = Vector2()
private val createLineVertices_V2 = Vector2()
private val createLineVertices_V1O = Vector2()
private val createLineVertices_V2O = Vector2()
fun createLineVertices(x1:Float, y1:Float, x2:Float, y2:Float, region: TextureRegion, color:Float = Color.WHITE.toFloatBits(), w1:Float = 1f, w2:Float = w1):FloatArray {
    val v1 = createLineVertices_V1.set(x1, y1)
    val v2 = createLineVertices_V2.set(x2, y2)

    val v1o = createLineVertices_V1O.set(v1).sub(v2).rotate90(1).nor()
    val v2o = createLineVertices_V2O.set(v1o)

    v1o.scl(w1)
    v2o.scl(w2)

    val vertices = createLineVertices_Vertices
    vertices[0] = v1.x - v1o.x
    vertices[1] = v1.y - v1o.y
    vertices[2] = color
    vertices[3] = region.u //TODO UV Coordinates are probably assigned incorrectly, because I don't really care now
    vertices[4] = region.v

    vertices[5] = v1.x + v1o.x
    vertices[6] = v1.y + v1o.y
    vertices[7] = color
    vertices[8] = region.u2
    vertices[9] = region.v

    vertices[10] = v2.x + v2o.x
    vertices[11] = v2.y + v2o.y
    vertices[12] = color
    vertices[13] = region.u2
    vertices[14] = region.v2

    vertices[15] = v2.x - v2o.x
    vertices[16] = v2.y - v2o.y
    vertices[17] = color
    vertices[18] = region.u
    vertices[19] = region.v2

    return vertices
}