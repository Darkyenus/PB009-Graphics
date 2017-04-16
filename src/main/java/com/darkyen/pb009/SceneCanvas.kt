package com.darkyen.pb009

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Disposable
import com.darkyen.pb009.presentations.ceil

/**
 *
 */
@Suppress("LeakingThis")
abstract class SceneCanvas : Table(Main.skin), Disposable {

    private val canvas = FrameBufferCanvas(this)
    private val optionsTable = Table(Main.skin)
    protected val input = InputMultiplexer()

    var mouseX:Int = 0
        private set
    var mouseY:Int = 0
        private set

    var screenWidth:Int = 1
        private set

    var screenHeight:Int = 1
        private set

    init {
        add(canvas).grow().row()
        isTransform = false

        optionsTable.defaults().pad(10f)
        optionsTable.background = Main.skin.newDrawable("white", Color.GRAY)
    }

    fun <SelectItem>newSelectBox(vararg items:SelectItem, changed:((SelectItem) -> Unit)? = null):() -> SelectItem {
        val selectBox = SelectBox<SelectItem>(Main.skin)
        selectBox.items.addAll(*items)
        selectBox.selected = items[0]
        if (changed != null) {
            selectBox.addListener(object : ChangeListener() {
                var selectedBefore = selectBox.selected

                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    val selectedNow = selectBox.selected
                    if (selectedNow != selectedBefore) {
                        selectedBefore = selectedNow
                        changed(selectedNow)
                    }
                }
            })
        }

        optionsTable.add(selectBox)

        return { selectBox.selected }
    }

    abstract fun render()

    private class FrameBufferCanvas(val parent:SceneCanvas) : Widget() {
        var framebuffer:FrameBuffer? = null

        init {
            addListener(object : InputListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    parent.input.touchUp(x.toInt(), y.toInt(), pointer, button)
                }

                override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    return parent.input.mouseMoved(x.toInt(), y.toInt())
                }

                override fun keyTyped(event: InputEvent?, character: Char): Boolean {
                    return parent.input.keyTyped(character)
                }

                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    return parent.input.touchDown(x.toInt(), y.toInt(), pointer, button)
                }

                override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean {
                    return parent.input.scrolled(amount)
                }

                override fun keyUp(event: InputEvent?, keycode: Int): Boolean {
                    return parent.input.keyUp(keycode)
                }

                override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    parent.input.touchDragged(x.toInt(), y.toInt(), pointer)
                }

                override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                    return parent.input.keyDown(keycode)
                }
            })
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            validate()
            stage.keyboardFocus = this
            stage.scrollFocus = this

            val width = ceil(width)
            val height = ceil(height)

            val density = Gdx.graphics.backBufferWidth / Gdx.graphics.width

            var framebuffer:FrameBuffer? = framebuffer
            if (framebuffer == null) {
                framebuffer = FrameBuffer(Pixmap.Format.RGBA8888, width * density, height * density, true, false)
                this.framebuffer = framebuffer
                parent.screenWidth = width
                parent.screenHeight = height
            }

            batch.end()
            framebuffer.begin()
            parent.render()
            framebuffer.end()
            batch.begin()

            batch.color = Color.WHITE

            val fbTexture = framebuffer.colorBufferTexture
            batch.draw(fbTexture, this.x, this.y, this.width, this.height)
        }

        override fun sizeChanged() {
            super.sizeChanged()
            framebuffer?.dispose()
            framebuffer = null
        }
    }

    override fun dispose() {
        canvas.framebuffer?.dispose()
    }
}