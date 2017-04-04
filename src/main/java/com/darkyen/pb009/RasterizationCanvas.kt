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
    private fun animationStepDuration():Float = (Math.log10(steps.size+1.0)+1.0).toFloat() / steps.size

    private var animationNextStepTimeout = animationStepDuration()

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
            animationNextStepTimeout = animationStepDuration()
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
                animationStep = -1
                stopButton.isChecked = false
            }
        })
        oneStepButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                setAnimating(false)
                if (animationStep + 1 == steps.size) {
                    animationStep = 0
                } else {
                    animationStep += 1
                }
                oneStepButton.isChecked = false
            }
        })

        if (variations.size > 1) {
            val variationSelection:SelectBox<VariationType> = object:SelectBox<VariationType>(Main.skin) {
                override fun toString(obj: VariationType): String {
                    return obj.toString().replace('_', ' ')
                }
            }
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
                canvas.drawElementCount = 0
                animationSlider.value = 1f
            }else {
                canvas.drawElementCount = steps[field]
                animationSlider.value = field.toFloat()
            }
        }

    override fun act(delta: Float) {
        super.act(delta)

        if (animating) {
            animationSlider_ignoreChangeEvent = true

            animationNextStepTimeout -= delta
            while (animationNextStepTimeout < 0) {
                animationNextStepTimeout += animationStepDuration()
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

            canvas.drawElements.clear()
            canvas.drawElementCount = 0
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

    abstract fun drawRaster(variant:VariationType)

    fun pixel(x:Int, y:Int, color:Color) {
        pixel(x, y, color.toFloatBits())
    }

    fun pixel(x:Int, y:Int, color:Float) {
        canvas.drawElements.add(DrawElement.Pixel(x, y, color))
    }

    fun line(x1:Float, y1:Float, x2:Float, y2:Float, w1: Float = 1f, w2: Float = w1, color: Float) {
        canvas.drawElements.add(DrawElement.Line(x1, y1, x2, y2, w1, w2, color))
    }

    fun step() {
        steps.add(canvas.drawElements.size)
    }

    fun newHandle(x:Float, y:Float, color: Color, direction: PointDirection = PointDirection.PointUpLeft):Handle {
        val handle = RasterCanvas.VirtualHandle(x, y, color, direction.left, direction.up)
        canvas.handles.add(handle)
        dirty = true
        return handle
    }

    protected class RasterCanvas(val parent:RasterizationCanvas<*>) : WidgetGroup() {
        var viewX:Float = 0f
        var viewY:Float = 0f
        var pixelSize:Float = 10f

        val drawElements = ObjectsArray<DrawElement>()
        var drawElementCount = 0

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
            val white = white
            val drawElements = drawElements

            val viewX = viewX
            val viewY = viewY
            val pixelSize = pixelSize
            val halfWidth = width/2
            val halfHeight = height/2

            fun tX(x:Float):Float {
                return (x - viewX) * pixelSize + halfWidth
            }

            fun tY(y:Float):Float {
                return (y - viewY) * pixelSize + halfHeight
            }

            for (i in 0..(drawElementCount - 1)) {
                val element = drawElements[i]

                when (element) {
                    is DrawElement.Pixel -> {
                        val (pX, pY, color) = element
                        batch.setColor(color)
                        batch.draw(white, tX(pX.toFloat()), tY(pY.toFloat()), pixelSize, pixelSize)
                    }
                    is DrawElement.Line -> {
                        val (x1, y1, x2, y2, w1, w2, color) = element
                        val vertices = createLineVertices(tX(x1), tY(y1), tX(x2), tY(y2), white, color, (w1/10f)*pixelSize, (w2/10f)*pixelSize)
                        batch.draw(white.texture, vertices, 0, vertices.size)
                    }
                }
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

        class VirtualHandle(x:Float, y:Float, override var color:Color, val left:Boolean, val up:Boolean) : Handle {
            override fun pixelX(): Int = round(handleX)

            override fun pixelY(): Int = round(handleY)

            override fun canvasX(): Float = handleX + 0.5f

            override fun canvasY(): Float = handleY + 0.5f

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
        fun pixelX():Int
        fun pixelY():Int
        fun canvasX():Float
        fun canvasY():Float

        var color:Color
    }

    companion object {
        val white:TextureRegion = Main.skin.getRegion("white")
        val handleImage: TextureRegion = Main.skin.getRegion("pixel-pin")
    }

    sealed class DrawElement {
        data class Pixel(val x:Int, val y:Int, val color:Float) : DrawElement()
        data class Line(val x1:Float, val y1:Float, val x2:Float, val y2:Float, val w1:Float, val w2:Float, val color:Float) : DrawElement()
    }
}