package com.darkyen.pb009

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