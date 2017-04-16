package com.darkyen.pb009.presentations

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.RenderContext
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.UBJsonReader
import com.darkyen.pb009.SceneCanvas

/**
 *
 */
class MaterialLaboratory : SceneCanvas() {

    val camera = PerspectiveCamera(90f, 1f, 1f)

    val modelBatch = ModelBatch()

    val models:ObjectMap<String, Model> = ObjectMap<String, Model>().apply {
        val loader = G3dModelLoader(UBJsonReader())
        put("Cube", loader.loadModel(Gdx.files.internal("cube.g3db")).apply {
            val material = materials.first()
            material.clear()
            material.set(ColorAttribute.createDiffuse(0.75164f, 0.60648f, 0.22648f, 1f),
                    ColorAttribute.createAmbient(0.24725f, 0.1995f, 0.0745f, 1f),
                    ColorAttribute.createSpecular(0.628281f, 0.555802f, 0.366065f, 1f),
                    FloatAttribute.createShininess(51f))
            materials.first().set(DepthTestAttribute(GL20.GL_LEQUAL, 0.1f, 100f))
        })
        put("Sphere", loader.loadModel(Gdx.files.internal("sphere.g3db")))
        put("Monkey", loader.loadModel(Gdx.files.internal("monkey.g3db")))

        val modelBuilder = ModelBuilder()
        put("Box", modelBuilder.createBox(1f, 1f, 1f,
                Material(ColorAttribute.createDiffuse(Color.GREEN)),
                (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()))
    }

    var currentModelInstance = ModelInstance(models["Cube"])

    init {
        camera.viewportWidth = screenWidth.toFloat()
        camera.viewportHeight = screenHeight.toFloat()
        camera.near = 1f
        camera.far = 100f
        camera.update()

        input.addProcessor(object :InputAdapter() {
            var rotation = 0f
            var heightAngle = 45f
            var distance = 3f

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                rotation += -Gdx.input.deltaX * 0.5f
                heightAngle = MathUtils.clamp(heightAngle + Gdx.input.deltaY * 0.5f, -89.9f, 89.9f)

                camera.position.set(
                        MathUtils.cosDeg(rotation) * MathUtils.cosDeg(heightAngle),
                        MathUtils.sinDeg(rotation) * MathUtils.cosDeg(heightAngle),
                        -MathUtils.sinDeg(heightAngle)
                ).scl(distance)
                camera.up.set(Vector3.Z)
                camera.lookAt(0f, 0f, 0f)
                camera.update()

                return true
            }

            override fun scrolled(amount: Int): Boolean {
                distance = MathUtils.clamp(distance + amount * 0.1f, 1f, 10f)
                touchDragged(0, 0, 0)
                return true
            }
        })
        input.touchDragged(0, 0, 0)
    }

    private val shader:MaterialShader = MaterialShader()
    private var initialized = false

    override fun render() {
        Gdx.gl.glClearColor(mouseX.toFloat() / screenWidth, mouseY.toFloat() / screenHeight, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if (!initialized) {
            initialized = true
            shader.init()
        }


        modelBatch.begin(camera)
        modelBatch.render(currentModelInstance, shader)
        modelBatch.end()
    }

    override fun dispose() {
        super.dispose()
        modelBatch.dispose()
        for (model in models.values()) {
            model.dispose()
        }
    }

    class MaterialShader : Shader {
        override fun canRender(instance: Renderable?): Boolean = true

        override fun compareTo(other: Shader?): Int = 0

        private lateinit var shader:ShaderProgram

        override fun init() {
            shader = ShaderProgram(Gdx.files.internal("material.vert"), Gdx.files.internal("material.frag"))
        }

        override fun begin(camera: Camera?, context: RenderContext?) {
            shader.begin()
        }

        override fun render(renderable: Renderable) {
            renderable.meshPart.render(shader, false)
        }

        override fun end() {
            shader.end()
        }

        override fun dispose() {
            shader.dispose()
        }

    }
}