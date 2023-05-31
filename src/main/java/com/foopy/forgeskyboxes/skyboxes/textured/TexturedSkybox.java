package com.foopy.forgeskyboxes.skyboxes.textured;

import com.mojang.blaze3d.systems.RenderSystem;
import com.foopy.forgeskyboxes.api.skyboxes.RotatableSkybox;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.skyboxes.AbstractSkybox;
import com.foopy.forgeskyboxes.util.object.*;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

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
        double timeRotationX = this.rotation.getRotationSpeedX() != 0F ? this.rotation.getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedX()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedX()), 1))) : 0D;
        double timeRotationY = this.rotation.getRotationSpeedY() != 0F ? this.rotation.getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedY()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedY()), 1))) : 0D;
        double timeRotationZ = this.rotation.getRotationSpeedZ() != 0F ? this.rotation.getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedZ()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.rotation.getRotationSpeedZ()), 1))) : 0D;
        this.applyTimeRotation(matrices, (float) timeRotationX, (float) timeRotationY, (float) timeRotationZ);
        // static
        matrices.mulPose(Vector3f.XP.rotationDegrees(rotationStatic.x()));
        matrices.mulPose(Vector3f.YP.rotationDegrees(rotationStatic.y()));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(rotationStatic.z()));
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

        matrices.mulPose(Vector3f.XP.rotationDegrees(timeRotationAxis.x()));
        matrices.mulPose(Vector3f.YP.rotationDegrees(timeRotationAxis.y()));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(timeRotationAxis.z()));
        matrices.mulPose(Vector3f.XP.rotationDegrees(timeRotationX));
        matrices.mulPose(Vector3f.YP.rotationDegrees(timeRotationY));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(timeRotationZ));
        matrices.mulPose(Vector3f.ZN.rotationDegrees(timeRotationAxis.z()));
        matrices.mulPose(Vector3f.YN.rotationDegrees(timeRotationAxis.y()));
        matrices.mulPose(Vector3f.XN.rotationDegrees(timeRotationAxis.x()));
    }

    public Blend getBlend() {
        return this.blend;
    }

    public Rotation getRotation() {
        return this.rotation;
    }
}
