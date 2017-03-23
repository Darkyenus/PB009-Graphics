package com.darkyen.pb009

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ObjectMap

/**
 *
 */
object Main : Game() {

    lateinit var batch: Batch
        private set
    lateinit var skin: Skin
        private set

    override fun create() {
        batch = SpriteBatch()
        assetManager.load("UISkin.json", Skin::class.java)
        assetManager.finishLoading()

        skin = assetManager.get<Skin>("UISkin.json")

        setScreen(MainScreen())
    }
}

val ARGS = ObjectMap<String, String>()

val assetManager = AssetManager(LocalFileHandleResolver())

fun main(args: Array<String>) {
    Box2D.init()
    val c = Lwjgl3ApplicationConfiguration()
    c.setTitle("PB009")
    c.useVsync(true)
    c.setResizable(true)
    c.setWindowedMode(800, 600)
    c.setWindowSizeLimits(200, 150, 40000, 30000)

    ARGS.ensureCapacity(args.size)
    for (arg in args) {
        val splitIndex = arg.indexOf(':')
        if (splitIndex == -1) {
            ARGS.put(arg, null)
        } else {
            ARGS.put(arg.substring(0, splitIndex), arg.substring(splitIndex + 1))
        }
    }

    Lwjgl3Application(Main, c)
}