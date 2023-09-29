package com.foopy.forgeskyboxes.skyboxes.textured;

import com.mojang.blaze3d.systems.RenderSystem;
import com.foopy.forgeskyboxes.api.skyboxes.RotatableSkybox;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.skyboxes.AbstractSkybox;
import com.foopy.forgeskyboxes.util.Utils;
import com.foopy.forgeskyboxes.util.object.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

public abstract class TexturedSkybox extends AbstractSkybox implements RotatableSkybox {
    public Rotation rotation;
    public Blend blend;

    protected TexturedSkybox() {
    }

    protected TexturedSkybox(Properties properties, Conditions conditions, Decorations decorations, Blend blend) {
        super(properties, conditions, decorations);
        this.blend = blend;
        this.rotation = properties.getRotation();
    }

    /**
     * Overrides and makes final here as there are options that should always be respected in a textured skybox.
     *
     * @param worldRendererAccess Access to the worldRenderer as skyboxes often require it.
     * @param matrices            The current PoseStack.
     * @param tickDelta           The current tick delta.
     */
    @Override
    public final void render(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.blend.applyBlendFunc(this.alpha);

        ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);

        Vector3f rotationStatic = this.rotation.getStatic();

        matrices.pushPose();

        // axis + time rotation
        double timeRotationX = Utils.calculateRotation(this.rotation.getRotationSpeedX(), this.rotation.getTimeShift().x(), this.rotation.getSkyboxRotation(), world);
        double timeRotationY = Utils.calculateRotation(this.rotation.getRotationSpeedY(), this.rotation.getTimeShift().y(), this.rotation.getSkyboxRotation(), world);
        double timeRotationZ = Utils.calculateRotation(this.rotation.getRotationSpeedZ(), this.rotation.getTimeShift().z(), this.rotation.getSkyboxRotation(), world);
        this.applyTimeRotation(matrices, (float) timeRotationX, (float) timeRotationY, (float) timeRotationZ);
        // static
        matrices.mulPose(Axis.XP.rotationDegrees(rotationStatic.x()));
        matrices.mulPose(Axis.YP.rotationDegrees(rotationStatic.y()));
        matrices.mulPose(Axis.ZP.rotationDegrees(rotationStatic.z()));
        this.renderSkybox(worldRendererAccess, matrices, tickDelta, camera, thickFog);
        matrices.popPose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        this.renderDecorations(worldRendererAccess, matrices, matrix4f, tickDelta, bufferBuilder, this.alpha);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        // fixme:
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Override this method instead of render if you are extending this skybox.
     */
    public abstract void renderSkybox(WorldRendererAccess worldRendererAccess, PoseStack matrices, float tickDelta, Camera camera, boolean thickFog);

    private void applyTimeRotation(PoseStack matrices, float timeRotationX, float timeRotationY, float timeRotationZ) {
        // Very ugly, find a better way to do this
        Vector3f timeRotationAxis = this.rotation.getAxis();
        matrices.mulPose(Axis.XP.rotationDegrees(timeRotationAxis.x()));
        matrices.mulPose(Axis.YP.rotationDegrees(timeRotationAxis.y()));
        matrices.mulPose(Axis.ZP.rotationDegrees(timeRotationAxis.z()));
        matrices.mulPose(Axis.XP.rotationDegrees(timeRotationX));
        matrices.mulPose(Axis.YP.rotationDegrees(timeRotationY));
        matrices.mulPose(Axis.ZP.rotationDegrees(timeRotationZ));
        matrices.mulPose(Axis.ZN.rotationDegrees(timeRotationAxis.z()));
        matrices.mulPose(Axis.YN.rotationDegrees(timeRotationAxis.y()));
        matrices.mulPose(Axis.XN.rotationDegrees(timeRotationAxis.x()));
    }

    public Blend getBlend() {
        return this.blend;
    }

    public Rotation getRotation() {
        return this.rotation;
    }
}
