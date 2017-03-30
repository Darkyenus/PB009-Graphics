package com.darkyen.pb009.presentations

import com.badlogic.gdx.graphics.Color

/**
 *
 */
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
fun hsv(hue:Float, saturation:Float = 1f, value:Float = 1f):Float {
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

    return color_TMP.set(r, g, b, 1f).toFloatBits()
}

fun rgb(r:Float, g:Float, b:Float):Float {
    return color_TMP.set(r, g, b, 1f).toFloatBits()
}

fun withAlpha(color:Float, newAlpha:Float):Float {
    val bits = java.lang.Float.floatToRawIntBits(color) and (255 shl 24).inv() or (((newAlpha * 255).toInt() and 255) shl 24)
    return java.lang.Float.intBitsToFloat(bits)
}