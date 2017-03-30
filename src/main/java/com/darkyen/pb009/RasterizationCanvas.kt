package com.darkyen.pb009

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.darkyen.pb009.presentations.*
import com.badlogic.gdx.utils.FloatArray as FloatsArray
import com.badlogic.gdx.utils.IntArray as IntsArray
import com.badlogic.gdx.utils.Array as ObjectsArray

/**
 *
 */
@Suppress("LeakingThis")
abstract class RasterizationCanvas<in VariationType>(variations:Array<VariationType>) : Table(Main.skin) {

    protected val canvas = RasterCanvas(this)
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
                playPauseButton.isChecked = false
            }
        })
        stopButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(false)
                animationStep = 0
                stopButton.isChecked = false
            }
        })
        oneStepButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(false)
                animationStep += 1
                oneStepButton.isChecked = false
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
        stage.scrollFocus = canvas

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

    fun newHandle(x:Float, y:Float, color: Color, left:Boolean = true, up:Boolean = true):Handle {
        val handle = RasterCanvas.VirtualHandle(x, y, color, left, up)
        canvas.handles.add(handle)
        dirty = true
        return handle
    }

    protected class RasterCanvas(val parent:RasterizationCanvas<*>) : WidgetGroup() {
        var viewX:Float = 0f
        var viewY:Float = 0f
        var pixelSize:Float = 10f

        val pixelsX = IntsArray()
        val pixelsY = IntsArray()
        val pixelsColor = FloatsArray()
        var pixels = 0

        private val widgetAreaBounds = Rectangle()
        private val scissorBounds = Rectangle()

        val handles = ObjectsArray<VirtualHandle>()

        private var dragging = false
        private var draggingHandle:VirtualHandle? = null

        init {
            addListener(object : InputListener() {
                var dragLastX = 0f
                var dragLastY = 0f

                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    if (pointer != Input.Buttons.LEFT) return false
                    dragLastX = x
                    dragLastY = y
                    dragging = true
                    draggingHandle = handleAt(x, y)
                    return true
                }

                override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                    val offsetX = (dragLastX - x) / pixelSize
                    val offsetY = (dragLastY - y) / pixelSize
                    dragLastX = x
                    dragLastY = y
                    parent.dirty = true

                    val handle = draggingHandle
                    if (handle != null) {
                        handle.handleX -= offsetX
                        handle.handleY -= offsetY
                    } else {
                        viewX += offsetX
                        viewY += offsetY
                    }
                }

                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    dragging = false
                    draggingHandle = null
                }

                override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean {
                    val newPixelSize = MathUtils.clamp(pixelSize + amount/10f, 0.5f, 50f)
                    if (pixelSize != newPixelSize) {
                        pixelSize = newPixelSize
                        parent.dirty = true
                        return true
                    }
                    return false
                }
            })
        }

        private fun handleAt(sceneX:Float, sceneY:Float):VirtualHandle? {
            val v = Vector2(sceneX, sceneY)

            val viewX = viewX
            val viewY = viewY
            val pixelSize = pixelSize
            val halfWidth = width/2
            val halfHeight = height/2

            for (handle in handles) {
                val rect = handle.touchRectangle(viewX, viewY, pixelSize, halfWidth, halfHeight)
                if (rect.contains(v)) {
                    return handle
                }
            }

            return null
        }

        override fun layout() {
            widgetAreaBounds.set(0f, 0f, width, height)
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            validate()
            applyTransform(batch, computeTransform())

            stage.calculateScissors(widgetAreaBounds, scissorBounds)
            batch.flush()
            if (ScissorStack.pushScissors(scissorBounds)) {
                drawCanvas(batch)
                drawChildren(batch, parentAlpha)
                batch.flush()
                ScissorStack.popScissors()
            }

            resetTransform(batch)
        }

        fun drawCanvas(batch: Batch) {
            val pixelsX = pixelsX
            val pixelsY = pixelsY
            val pixelsColor = pixelsColor

            val viewX = viewX
            val viewY = viewY
            val pixelSize = pixelSize
            val halfWidth = width/2
            val halfHeight = height/2

            for (i in 0..(pixels - 1)) {
                val pX = pixelsX[i]
                val pY = pixelsY[i]
                val pColor = pixelsColor[i]

                batch.setColor(pColor)
                batch.draw(white, (pX - viewX) * pixelSize + halfWidth, (pY - viewY) * pixelSize + halfHeight, pixelSize, pixelSize)
            }

            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            this.screenToLocalCoordinates(mouse)
            val mouseOver:VirtualHandle? = handleAt(mouse.x, mouse.y)

            for (handle in handles) {
                if (dragging && draggingHandle == handle) {
                    batch.setColor(handle.color.r * 0.5f, handle.color.g * 0.5f, handle.color.b * 0.5f, 1f)
                } else if (!dragging && mouseOver == handle) {
                    batch.setColor(handle.color.r * 0.85f, handle.color.g * 0.85f, handle.color.b * 0.85f, 1f)
                } else {
                    batch.color = handle.color
                }

                handle.draw(batch, viewX, viewY, pixelSize, halfWidth, halfHeight)
            }

            batch.color = Color.WHITE
        }

        class VirtualHandle(x:Float, y:Float, val color:Color, val left:Boolean, val up:Boolean) : Handle {
            override fun canvasX(): Int = round(handleX)

            override fun canvasY(): Int = round(handleY)

            var handleX:Float = x
            var handleY:Float = y

            val width:Float = handleImage.regionWidth.toFloat()
            val height:Float = handleImage.regionHeight.toFloat()

            fun touchRectangle(viewX:Float, viewY:Float, pixelSize: Float, halfWidth:Float, halfHeight:Float):Rectangle {
                val rect = drawRect(viewX, viewY, pixelSize, halfWidth, halfHeight)
                if (rect.width < 0) {
                    rect.width = -rect.width
                    rect.x -= rect.width
                }

                if (rect.height < 0) {
                    rect.height = -rect.height
                    rect.y -= rect.height
                }

                return rect
            }


            private val drawRect_TMP = Rectangle()
            private fun drawRect(viewX:Float, viewY:Float, pixelSize: Float, halfWidth:Float, halfHeight:Float):Rectangle {
                val drawX:Float
                val drawWidth:Float
                if (left) {
                    drawX = (this.handleX - viewX + 0.5f) * pixelSize + halfWidth
                    drawWidth = this.width
                } else {
                    drawX = (this.handleX - viewX + 0.5f) * pixelSize + halfWidth
                    drawWidth = -this.width
                }

                val drawY:Float
                val drawHeight:Float
                if (up) {
                    drawY = (this.handleY - viewY + 0.5f) * pixelSize + halfHeight - this.height
                    drawHeight = this.height
                } else {
                    drawY = (this.handleY - viewY + 0.5f) * pixelSize + halfHeight + this.height
                    drawHeight = -this.height
                }

                return drawRect_TMP.set(drawX, drawY, drawWidth, drawHeight)
            }

            fun draw(batch: Batch, viewX:Float, viewY:Float, pixelSize: Float, halfWidth:Float, halfHeight:Float) {
                val rect = drawRect(viewX, viewY, pixelSize, halfWidth, halfHeight)
                batch.draw(handleImage, rect.x, rect.y, rect.width, rect.height)

                //batch.draw(white, (this.handleX - viewX) * pixelSize + halfWidth, (this.handleY - viewY) * pixelSize + halfHeight, pixelSize, pixelSize)
            }
        }
    }

    interface Handle {
        fun canvasX():Int
        fun canvasY():Int
    }

    companion object {
        val white:TextureRegion = Main.skin.getRegion("white")
        val handleImage: TextureRegion = Main.skin.getRegion("pixel-pin")
    }
}