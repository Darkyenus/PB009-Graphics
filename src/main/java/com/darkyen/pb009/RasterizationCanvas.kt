package com.darkyen.pb009

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.FloatArray as FloatsArray
import com.badlogic.gdx.utils.IntArray as IntsArray

/**
 *
 */
@Suppress("LeakingThis")
abstract class RasterizationCanvas<in VariationType>(variations:Array<VariationType>) : Table(Main.skin) {

    private val canvas = RasterCanvas(this)
    private var dirty = true

    private val steps = IntsArray()
    private val animationSlider = Slider(0f, 0f, 1f, false, Main.skin)
    private var animationSlider_ignoreChangeEvent = false
    private var animating = false
    private val animationStepDuration = 0.10f

    private var animationNextStepTimeout = animationStepDuration

    private var currentVariation:VariationType = variations[0]

    init {
        add(canvas).grow().row()

        val animationControlTable = Table(Main.skin)
        animationControlTable.defaults().pad(10f)
        animationControlTable.background = Main.skin.newDrawable("white", Color.GRAY)

        val playPauseButton = imageButton("play-button")
        val stopButton = imageButton("stop-button")
        val oneStepButton = imageButton("step-button")

        fun setAnimating(animating:Boolean) {
            this.animating = animating
            animationNextStepTimeout = animationStepDuration
        }

        animationSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (animationSlider_ignoreChangeEvent) return

                setAnimating(false)
                animationStep = Math.round(animationSlider.value)
            }
        })

        playPauseButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(!animating)
            }
        })
        stopButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(false)
                animationStep = 0
            }
        })
        oneStepButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(false)
                animationStep += 1
            }
        })

        if (variations.size > 1) {
            val variationSelection = SelectBox<VariationType>(Main.skin)
            variationSelection.setItems(*variations)
            variationSelection.selectedIndex = 0

            variationSelection.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    currentVariation = variationSelection.selected
                    dirty = true
                }
            })

            animationControlTable.add(variationSelection)

            addListener(object : InputListener() {
                override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                    if (variationSelection.items.size <= 1) return false
                    when (keycode) {
                        Input.Keys.UP, Input.Keys.W -> {
                            variationSelection.selectedIndex = (variationSelection.selectedIndex + 1) % variationSelection.items.size
                            return true
                        }
                        Input.Keys.DOWN, Input.Keys.S -> {
                            variationSelection.selectedIndex = (variationSelection.selectedIndex - 1 + variationSelection.items.size) % variationSelection.items.size
                            return true
                        }
                    }
                    return false
                }
            })
        }
        animationControlTable.add(animationSlider).growX()
        animationControlTable.add(playPauseButton)
        animationControlTable.add(stopButton)
        animationControlTable.add(oneStepButton)

        add(animationControlTable).growX()
    }

    var animationStep:Int = 0
        set(value) {
            if (value < 0) {
                field = maxOf(steps.size + value, 0)
            } else {
                field = minOf(steps.size - 1, value)
            }

            if (steps.size == 0) {
                canvas.pixels = 0
                animationSlider.value = 1f
            }else {
                canvas.pixels = steps[field]
                animationSlider.value = field.toFloat()
            }
        }

    override fun act(delta: Float) {
        super.act(delta)

        if (animating) {
            animationSlider_ignoreChangeEvent = true

            animationNextStepTimeout -= delta
            while (animationNextStepTimeout < 0) {
                animationNextStepTimeout += animationStepDuration
                if (animationStep == steps.size - 1) {
                    animationStep = 0
                } else {
                    animationStep += 1
                }
            }

            animationSlider_ignoreChangeEvent = false
        }
    }

    override fun validate() {
        super.validate()

        stage.keyboardFocus = this

        if (dirty) {
            dirty = false

            val previousAnimationStep = animationStep

            canvas.pixelsX.clear()
            canvas.pixelsY.clear()
            canvas.pixelsColor.clear()
            canvas.pixels = 0
            steps.clear()

            drawRaster(currentVariation)

            animationSlider.setRange(0f, maxOf(steps.size - 1, 0).toFloat())
            if (animating) {
                animationStep = previousAnimationStep
                animating = true
            } else {
                animationStep = -1
            }
        }
    }

    override fun invalidate() {
        dirty = true
        super.invalidate()
    }

    abstract fun drawRaster(variation:VariationType)

    fun pixel(x:Int, y:Int, color:Color, step:Boolean = true) {
        pixel(x, y, color.toFloatBits(), step)
    }

    fun pixel(x:Int, y:Int, color:Float, step:Boolean = true) {
        canvas.pixelsX.add(x)
        canvas.pixelsY.add(y)
        canvas.pixelsColor.add(color)
        if (step) {
            step()
        }
    }

    fun step() {
        steps.add(canvas.pixelsX.size)
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

    fun newHandle(color: Color):Handle {
        val handle = HandleWidget(canvas, color)
        handle.x = 100f
        handle.y = 100f * (canvas.children.size + 1)
        canvas.addActor(handle)
        dirty = true
        return handle
    }

    private class RasterCanvas(val parent:RasterizationCanvas<*>) : WidgetGroup() {
        var viewX:Float = 0f
        var viewY:Float = 0f
        var pixelSize:Float = 10f

        val white = Main.skin.getRegion("white")!!
        val pixelsX = IntsArray()
        val pixelsY = IntsArray()
        val pixelsColor = FloatsArray()
        var pixels = 0

        private val widgetAreaBounds = Rectangle()
        private val scissorBounds = Rectangle()

        override fun layout() {
            widgetAreaBounds.set(0f, 0f, width, height)
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            validate()
            applyTransform(batch, computeTransform())

            stage.calculateScissors(widgetAreaBounds, scissorBounds)
            batch.flush()
            if (ScissorStack.pushScissors(scissorBounds)) {
                drawPixels(batch)
                drawChildren(batch, parentAlpha)
                batch.flush()
                ScissorStack.popScissors()
            }

            resetTransform(batch)
        }

        fun drawPixels(batch: Batch) {
            val pixelsX = pixelsX
            val pixelsY = pixelsY
            val pixelsColor = pixelsColor

            val pixelSize = pixelSize
            val offX = viewX + width/2
            val offY = viewY + height/2

            for (i in 0..(pixels - 1)) {
                val pX = pixelsX[i]
                val pY = pixelsY[i]
                val pColor = pixelsColor[i]

                batch.setColor(pColor)
                batch.draw(white, pX * pixelSize + offX, pY * pixelSize + offY, pixelSize, pixelSize)
            }

            batch.color = Color.WHITE
        }
    }

    interface Handle {
        fun canvasX():Int
        fun canvasY():Int
    }

    private class HandleWidget(val parent:RasterCanvas, color: Color = Color.RED) : Widget(), Handle {
        init {
            setSize(prefWidth, prefHeight)
            setColor(color)

            addListener(object : InputListener() {
                var dragging = false
                var dragOffX = 0f
                var dragOffY = 0f

                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    if (pointer != Input.Buttons.LEFT) return false
                    dragOffX = x
                    dragOffY = y
                    return true
                }

                override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                    this@HandleWidget.x += x - dragOffX
                    this@HandleWidget.y += y - dragOffY
                    parent.parent.dirty = true
                }

                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    dragging = false
                }
            })
        }

        override fun canvasX():Int {
            return Math.round((x - parent.viewX - parent.width / 2f) / parent.pixelSize - 0.5f)
        }

        override fun canvasY():Int {
            return Math.round((y + height - parent.viewY - parent.height / 2f) / parent.pixelSize - 0.5f)
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            validate()

            val oldColor = batch.packedColor
            val color = color
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
            batch.draw(handleImage, x, y, width, height)

            batch.setColor(oldColor)
        }

        override fun getPrefWidth(): Float {
            return handleImage.regionWidth.toFloat()
        }

        override fun getPrefHeight(): Float {
            return handleImage.regionHeight.toFloat()
        }

        companion object {
            val handleImage: TextureRegion = Main.skin.getRegion("pixel-pin")
        }
    }
}