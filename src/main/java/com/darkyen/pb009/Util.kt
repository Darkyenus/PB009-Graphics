package com.darkyen.pb009

/**
 *
 */
fun signum(n:Int):Int {
    if (n < 0) return -1
    if (n > 0) return 1
    return 0
}

infix fun Float.posMod(f:Float):Float {
    val result = this % f
    if (result < 0) return result + f
    return result
}