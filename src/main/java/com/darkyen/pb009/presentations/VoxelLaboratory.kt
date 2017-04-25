package com.darkyen.pb009.presentations

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.math.MathUtils
import com.darkyen.pb009.ShaderCanvas
import com.darkyen.pb009.util.AutoReloadShaderProgram

/**
 *
 */
class VoxelLaboratory : ShaderCanvas() {

    val voxelShader = AutoReloadShaderProgram(Gdx.files.internal("voxel_vertex.glsl"), Gdx.files.internal("voxel_fragment.glsl")).apply {
        if (!isCompiled) {
            throw IllegalStateException("Shader did not compile: "+log)
        }
    }

    val locScreenDimensions = voxelShader.getUniformLocation("screenDimensions")
    val locCameraOrigin = voxelShader.getUniformLocation("cameraOrigin")
    val locCameraTarget = voxelShader.getUniformLocation("cameraTarget")
    val locTime = voxelShader.getUniformLocation("time")
    val locShadingLevel = voxelShader.getUniformLocation("shadingLevel")
    val locShape = voxelShader.getUniformLocation("shape")

    var cameraRotation = 45f
    var cameraHeightAngle = -45f
    var cameraDistance = 40f

    private val shadingLevel:() -> ShadingLevel = newSelectBox(*ShadingLevel.values(), initiallySelected = ShadingLevel.PhongLikeWithShadows.ordinal)
    private val voxelShape:() -> VoxelShape = newSelectBox(*VoxelShape.values())

    init {
        input.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                cameraRotation += Gdx.input.deltaX * 0.5f
                cameraHeightAngle = MathUtils.clamp(cameraHeightAngle - Gdx.input.deltaY * 0.5f, -89.9f, 89.9f)
                return true
            }

            override fun scrolled(amount: Int): Boolean {
                cameraDistance = MathUtils.clamp(cameraDistance * (1f + amount * 0.05f), 1f, 500f)
                return true
            }
        })
    }

    private var time = 0f

    override fun render() {
        time += Gdx.graphics.deltaTime

        Gdx.gl.glClearColor(mouseX.toFloat() / screenWidth, mouseY.toFloat() / screenHeight, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Gdx.gl.glDisable(GL20.GL_BLEND)
        //Gdx.gl.glDepthMask(false)
        voxelShader.begin()
        Gdx.gl.glUniform2f(locScreenDimensions, screenWidth.toFloat(), screenHeight.toFloat())
        Gdx.gl.glUniform3f(locCameraOrigin,
                MathUtils.cosDeg(cameraRotation) * MathUtils.cosDeg(cameraHeightAngle) * cameraDistance,
                -MathUtils.sinDeg(cameraHeightAngle) * cameraDistance,
                MathUtils.sinDeg(cameraRotation) * MathUtils.cosDeg(cameraHeightAngle) * cameraDistance
        )
        Gdx.gl.glUniform3f(locCameraTarget, 0f, 0f, 0f)
        Gdx.gl.glUniform1f(locTime, time)
        Gdx.gl.glUniform1i(locShadingLevel, shadingLevel().ordinal)
        Gdx.gl.glUniform1i(locShape, voxelShape().ordinal)

        screenMesh.render(voxelShader, GL20.GL_TRIANGLE_FAN, 0, 4)
        voxelShader.end()
        //Gdx.gl.glDepthMask(true)
    }

    private enum class ShadingLevel {
        Solid,
        PhongLike,
        PhongLikeWithShadows
    }

    private enum class VoxelShape {
        Spheres,
        Cubes,
        Torus,
        Plane,
        SkewedPlane,
        Cylinder
    }
}