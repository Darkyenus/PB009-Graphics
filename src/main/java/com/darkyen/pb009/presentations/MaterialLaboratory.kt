package com.darkyen.pb009.presentations

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelBatch
//import com.badlogic.gdx.graphics.g3d.ModelInstance
//import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
//import com.badlogic.gdx.utils.UBJsonReader
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.darkyen.pb009.SceneCanvas

/**
 *
 */
class MaterialLaboratory : SceneCanvas(ExtendViewport(10f, 10f, PerspectiveCamera(90f, 10f, 10f))) {

    val modelBatch = ModelBatch()

    //val bunny = G3dModelLoader(UBJsonReader()).loadModel(Gdx.files.internal("knight_statue.g3db"))

    //val bunnyRenderable = ModelInstance(bunny, 0f, 0f, 0f)

    override fun prepareCamera() {
        viewport.camera.position.set(10f, 10f, 10f)
        viewport.camera.lookAt(0f, 0f, 0f)
    }

    override fun render() {
        Gdx.gl.glClearColor(mouseX.toFloat() / screenWidth, mouseY.toFloat() / screenHeight, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        modelBatch.begin(viewport.camera)

        //modelBatch.render(bunnyRenderable)

        modelBatch.end()

    }
}