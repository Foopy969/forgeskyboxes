package com.foopy.forgeskyboxes.skyboxes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.util.object.*;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class MonoColorSkybox extends AbstractSkybox {
    public static Codec<MonoColorSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(Properties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getProperties), Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(AbstractSkybox::getConditions), Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations), RGBA.CODEC.optionalFieldOf("color", RGBA.DEFAULT).forGetter(MonoColorSkybox::getColor), Blend.CODEC.optionalFieldOf("blend", Blend.DEFAULT).forGetter(MonoColorSkybox::getBlend)).apply(instance, MonoColorSkybox::new));
    public RGBA color;
    public Blend blend;

    public MonoColorSkybox() {
    }

    public MonoColorSkybox(Properties properties, Conditions conditions, Decorations decorations, RGBA color, Blend blend) {
        super(properties, conditions, decorations);
        this.color = color;
        this.blend = blend;
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.MONO_COLOR_SKYBOX.get();
    }

    @Override
    public void render(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog) {
        if (this.alpha > 0) {
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            this.blend.applyBlendFunc(this.alpha);
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

            for (int i = 0; i < 6; ++i) {
                matrices.pushPose();
                if (i == 1) {
                    matrices.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                } else if (i == 2) {
                    matrices.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                    matrices.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                } else if (i == 3) {
                    matrices.mulPose(Vector3f.XP.rotationDegrees(180.0F));
                } else if (i == 4) {
                    matrices.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                    matrices.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                } else if (i == 5) {
                    matrices.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
                    matrices.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                }

                Matrix4f matrix4f = matrices.last().pose();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.vertex(matrix4f, -75.0F, -75.0F, -75.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).endVertex();
                bufferBuilder.vertex(matrix4f, -75.0F, -75.0F, 75.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).endVertex();
                bufferBuilder.vertex(matrix4f, 75.0F, -75.0F, 75.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).endVertex();
                bufferBuilder.vertex(matrix4f, 75.0F, -75.0F, -75.0F).color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha).endVertex();
                BufferUploader.drawWithShader(bufferBuilder.end());
                matrices.popPose();
            }

            this.renderDecorations(worldRendererAccess, matrices, projectionMatrix, tickDelta, bufferBuilder, this.alpha);

            RenderSystem.disableBlend();
        }
    }

    public RGBA getColor() {
        return this.color;
    }

    public Blend getBlend() {
        return blend;
    }
}
