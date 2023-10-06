package com.foopy.forgeskyboxes.skyboxes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.foopy.forgeskyboxes.SkyboxManager;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.util.object.Conditions;
import com.foopy.forgeskyboxes.util.object.Decorations;
import com.foopy.forgeskyboxes.util.object.Properties;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;



public class OverworldSkybox extends AbstractSkybox {
    public static Codec<OverworldSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(AbstractSkybox::getConditions),
            Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations)
    ).apply(instance, OverworldSkybox::new));

    public OverworldSkybox(Properties properties, Conditions conditions, Decorations decorations) {
        super(properties, conditions, decorations);
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.MONO_COLOR_SKYBOX.get();
    }

    @Override
    public void render(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        assert client.level != null;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        Vec3 vec3d = world.getSkyColor(client.gameRenderer.getMainCamera().getPosition(), tickDelta);
        float f = (float) vec3d.x;
        float g = (float) vec3d.y;
        float h = (float) vec3d.z;
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);

        // Light Sky
        RenderSystem.setShaderColor(f, g, h, this.alpha);
        ShaderInstance shaderProgram = RenderSystem.getShader();
        worldRendererAccess.getLightSkyBuffer().bind();
        worldRendererAccess.getLightSkyBuffer().drawWithShader(matrices.last().pose(), projectionMatrix, shaderProgram);
        VertexBuffer.unbind();


        RenderSystem.enableBlend();
        float skyAngle = world.getTimeOfDay(tickDelta);
        float skyAngleRadian = world.getSunAngle(tickDelta);

        if (SkyboxManager.getInstance().isEnabled() && SkyboxManager.getInstance().getActiveSkyboxes().stream().anyMatch(skybox -> skybox instanceof AbstractSkybox abstractSkybox && abstractSkybox.getDecorations().getRotation().getSkyboxRotation())) {
            skyAngle = Mth.positiveModulo(world.getDayTime()/ 24000F + 0.75F, 1);
            skyAngleRadian = skyAngle * (float) (Math.PI * 2);
        }

        float[] fs = world.effects().getSunriseColor(skyAngle, tickDelta);
        if (fs != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            matrices.pushPose();
            matrices.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            float i = Mth.sin(skyAngleRadian) < 0.0F ? 180.0F : 0.0F;
            matrices.mulPose(Vector3f.ZP.rotationDegrees(i));
            matrices.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            float j = fs[0];
            float k = fs[1];
            float l = fs[2];
            Matrix4f matrix4f = matrices.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(j, k, l, fs[3] * this.alpha).endVertex();

            for (int n = 0; n <= 16; ++n) {
                float o = (float) n * (float) (Math.PI * 2) / 16.0F;
                float p = Mth.sin(o);
                float q = Mth.cos(o);
                bufferBuilder.vertex(matrix4f, p * 120.0F, q * 120.0F, -q * 40.0F * fs[3]).color(fs[0], fs[1], fs[2], 0.0F).endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
            matrices.popPose();
        }


        this.renderDecorations(worldRendererAccess, matrices, projectionMatrix, tickDelta, bufferBuilder, this.alpha);

        // Dark Sky
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double d = client.player.getEyePosition(tickDelta).y - world.getLevelData().getHorizonHeight(world);
        if (d < 0.0) {
            matrices.pushPose();
            matrices.translate(0.0F, 12.0F, 0.0F);
            worldRendererAccess.getDarkSkyBuffer().bind();
            worldRendererAccess.getDarkSkyBuffer().drawWithShader(matrices.last().pose(), projectionMatrix, shaderProgram);
            VertexBuffer.unbind();
            matrices.popPose();
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
