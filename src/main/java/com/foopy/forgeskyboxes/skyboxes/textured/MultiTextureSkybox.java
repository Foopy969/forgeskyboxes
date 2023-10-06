package com.foopy.forgeskyboxes.skyboxes.textured;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.client.Camera;

import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.skyboxes.AbstractSkybox;
import com.foopy.forgeskyboxes.skyboxes.SkyboxType;
import com.foopy.forgeskyboxes.util.Utils;
import com.foopy.forgeskyboxes.util.object.*;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;



import java.util.ArrayList;
import java.util.List;

public class MultiTextureSkybox extends TexturedSkybox {
    public static Codec<MultiTextureSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(AbstractSkybox::getConditions),
            Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations),
            Blend.CODEC.optionalFieldOf("blend", Blend.DEFAULT).forGetter(TexturedSkybox::getBlend),
            Animation.CODEC.listOf().optionalFieldOf("animations", new ArrayList<>()).forGetter(MultiTextureSkybox::getAnimations)
    ).apply(instance, MultiTextureSkybox::new));
    protected final List<Animation> animations;
    private final UVRanges uvRanges;

    private final float quadSize = 100F;
    private final UVRange quad = new UVRange(-this.quadSize, -this.quadSize, this.quadSize, this.quadSize);

    public MultiTextureSkybox(Properties properties, Conditions conditions, Decorations decorations, Blend blend, List<Animation> animations) {
        super(properties, conditions, decorations, blend);
        this.animations = animations;
        this.uvRanges = Util.make(() -> new UVRanges(
                new UVRange(1.0F / 3.0F, 1.0F / 2.0F, 2.0F / 3.0F, 1),
                new UVRange(2.0F / 3.0F, 0, 1, 1.0F / 2.0F),
                new UVRange(2.0F / 3.0F, 1.0F / 2.0F, 1, 1),
                new UVRange(0, 1.0F / 2.0F, 1.0F / 3.0F, 1),
                new UVRange(1.0F / 3.0F, 0, 2.0F / 3.0F, 1.0F / 2.0F),
                new UVRange(0, 0, 1.0F / 3.0F, 1.0F / 2.0F)
        ));
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.MONO_COLOR_SKYBOX.get();
    }

    @Override
    public void renderSkybox(WorldRendererAccess worldRendererAccess, PoseStack matrices, float tickDelta, Camera camera, boolean thickFog) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < 6; ++i) {
            // 0 = bottom
            // 1 = north
            // 2 = south
            // 3 = top
            // 4 = east
            // 5 = west
            UVRange faceUVRange = this.uvRanges.byId(i);
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

            // animations
            for (Animation animation : this.animations) {
                animation.tick();
                UVRange intersect = Utils.findUVIntersection(faceUVRange, animation.getUvRanges()); // todo: cache this intersections so we don't waste gpu cycles
                if (intersect != null && animation.getCurrentFrame() != null) {
                    UVRange intersectionOnCurrentTexture = Utils.mapUVRanges(faceUVRange, this.quad, intersect);
                    UVRange intersectionOnCurrentFrame = Utils.mapUVRanges(animation.getUvRanges(), animation.getCurrentFrame(), intersect);

                    // Render the quad at the calculated position
                    RenderSystem.setShaderTexture(0, animation.getTexture().getTextureId());

                    bufferBuilder.vertex(matrix4f, intersectionOnCurrentTexture.getMinU(), -this.quadSize, intersectionOnCurrentTexture.getMinV()).uv(intersectionOnCurrentFrame.getMinU(), intersectionOnCurrentFrame.getMinV()).endVertex();
                    bufferBuilder.vertex(matrix4f, intersectionOnCurrentTexture.getMinU(), -this.quadSize, intersectionOnCurrentTexture.getMaxV()).uv(intersectionOnCurrentFrame.getMinU(), intersectionOnCurrentFrame.getMaxV()).endVertex();
                    bufferBuilder.vertex(matrix4f, intersectionOnCurrentTexture.getMaxU(), -this.quadSize, intersectionOnCurrentTexture.getMaxV()).uv(intersectionOnCurrentFrame.getMaxU(), intersectionOnCurrentFrame.getMaxV()).endVertex();
                    bufferBuilder.vertex(matrix4f, intersectionOnCurrentTexture.getMaxU(), -this.quadSize, intersectionOnCurrentTexture.getMinV()).uv(intersectionOnCurrentFrame.getMaxU(), intersectionOnCurrentFrame.getMinV()).endVertex();
                }
            }

            matrices.popPose();
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public List<Animation> getAnimations() {
        return animations;
    }
}
