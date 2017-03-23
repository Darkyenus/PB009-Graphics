package com.darkyen.pb009

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport

/**
 *
 */
class MainScreen : ScreenAdapter() {

    val viewport = ScreenViewport()
    val stage = Stage(viewport)

    val presentationContainer = Container<Actor>(null)

    init {
        val rootSplit = Table(Main.skin)
        rootSplit.setFillParent(true)
        stage.addActor(rootSplit)

        val selectorList = VerticalGroup()
        val selectorScroll = ScrollPane(selectorList, Main.skin)
        rootSplit.add(selectorScroll).fillY().expandY()

        presentationContainer.fill()
        rootSplit.add(presentationContainer).fill().expand()

        fun present(name:String, begin:() -> Actor) {
            val button = TextButton(name, Main.skin)
            button.addListener(object:ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    presentationContainer.actor = begin()
                }
            })
            button.align(Align.left)
            selectorList.addActor(button)
        }

        present("Line Rasterization") {
            LineRasterization()
        }

        present("Circle Rasterization") {
            CircleRasterization()
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        Gdx.gl.glEnable(GL20.GL_BLEND)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

}