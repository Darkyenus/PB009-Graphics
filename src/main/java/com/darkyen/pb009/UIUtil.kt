package com.darkyen.pb009

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton

/**
 *
 */

fun imageButton(image:String): Button {
    val base = Main.skin.getDrawable(image)
    val over = Main.skin.newDrawable(image, Color(0.8f, 0.8f, 0.8f, 1f))
    val pressed = Main.skin.newDrawable(image, Color(0.5f, 0.5f, 0.5f, 1f))
    return ImageButton(base, over, pressed)
}

enum class PointDirection(val left:Boolean, val up:Boolean) {
    PointUpLeft(true, true),
    PointDownLeft(true, false),
    PointUpRight(false, true),
    PointDownRight(false, false)
}