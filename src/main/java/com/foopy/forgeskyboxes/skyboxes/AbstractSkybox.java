package com.foopy.forgeskyboxes.skyboxes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.api.skyboxes.FSBSkybox;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.util.Constants;
import com.foopy.forgeskyboxes.util.Utils;
import com.foopy.forgeskyboxes.util.object.Conditions;
import com.foopy.forgeskyboxes.util.object.Decorations;
import com.foopy.forgeskyboxes.util.object.Properties;
import com.foopy.forgeskyboxes.util.object.Weather;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;

import java.util.Objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * All classes that implement {@link AbstractSkybox} should
 * have a default constructor as it is required when checking
 * the type of the skybox.
 */
public abstract class AbstractSkybox implements FSBSkybox {

    /**
     * The current alpha for the skybox. Expects all skyboxes extending this to accommodate this.
     * This variable is responsible for fading in/out skyboxes.
     */
    public transient float alpha;
    protected Properties properties;
    protected Conditions conditions = Conditions.DEFAULT;
    protected Decorations decorations = Decorations.DEFAULT;
    private Float fadeInDelta = null;
    private Float fadeOutDelta = null;

    protected AbstractSkybox() {
    }

    protected AbstractSkybox(Properties properties, Conditions conditions, Decorations decorations) {
        this.properties = properties;
        this.conditions = conditions;
        this.decorations = decorations;
    }

    /**
     * Calculates the alpha value for the current time and conditions and returns it.
     *
     * @return The new alpha value.
     */
    @Override
    public final float updateAlpha() {
        int currentTime = (int) (Objects.requireNonNull(Minecraft.getInstance().level).getDayTime() % 24000);

        boolean shouldRender = Utils.isInTimeInterval(currentTime, this.properties.getFade().getStartFadeIn(), this.properties.getFade().getStartFadeOut() - 1);

        if ((shouldRender || this.properties.getFade().isAlwaysOn()) && this.checkConditions()) {
            if (this.alpha < this.properties.getMaxAlpha()) {
                // Check if currentTime is at the beginning of fadeIn
                if (this.properties.getFade().getStartFadeIn() == currentTime && this.fadeInDelta == null) {
                    float f1 = Utils.normalizeTime(this.properties.getMaxAlpha(), currentTime, this.properties.getFade().getStartFadeIn(), this.properties.getFade().getEndFadeIn());
                    float f2 = Utils.normalizeTime(this.properties.getMaxAlpha(), currentTime + 1, this.properties.getFade().getStartFadeIn(), this.properties.getFade().getEndFadeIn());
                    this.fadeInDelta = f2 - f1;
                }

                this.alpha += Objects.requireNonNullElseGet(this.fadeInDelta, () -> this.properties.getMaxAlpha() / this.properties.getTransitionInDuration());
            } else {
                this.alpha = this.properties.getMaxAlpha();
                if (this.fadeInDelta != null) {
                    this.fadeInDelta = null;
                }
            }
        } else {
            if (this.alpha > 0f) {
                // Check if currentTime is at the beginning of fadeOut
                if (this.properties.getFade().getStartFadeOut() == currentTime && this.fadeOutDelta == null) {
                    float f1 = Utils.normalizeTime(this.properties.getMaxAlpha(), currentTime, this.properties.getFade().getStartFadeOut(), this.properties.getFade().getEndFadeOut());
                    float f2 = Utils.normalizeTime(this.properties.getMaxAlpha(), currentTime + 1, this.properties.getFade().getStartFadeOut(), this.properties.getFade().getEndFadeOut());
                    this.fadeOutDelta = f2 - f1;
                }

                this.alpha -= Objects.requireNonNullElseGet(this.fadeOutDelta, () -> this.properties.getMaxAlpha() / this.properties.getTransitionOutDuration());
            } else {
                this.alpha = 0F;
                if (this.fadeOutDelta != null) {
                    this.fadeOutDelta = null;
                }
            }
        }

        this.alpha = Mth.clamp(this.alpha, 0F, this.properties.getMaxAlpha());

        return this.alpha;
    }

    /**
     * @return Whether all conditions were met
     */
    protected boolean checkConditions() {
        return this.checkDimensions() && this.checkWorlds() && this.checkBiomes() && this.checkXRanges() && this.checkYRanges() && this.checkZRanges() && this.checkWeather() && this.checkEffects() && this.checkLoop();
    }

    /**
     * @return Whether the current biomes and dimensions are valid for this skybox.
     */
    protected boolean checkBiomes() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        Objects.requireNonNull(client.player);
        return this.conditions.getBiomes().isEmpty() || this.conditions.getBiomes().contains(client.level.registryAccess().registryOrThrow(Registries.BIOME).getKey(client.level.getBiome(client.player.blockPosition()).get()));
    }

    /**
     * @return Whether the current dimension ResourceLocation is valid for this skybox
     */
    protected boolean checkDimensions() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        return this.conditions.getDimensions().isEmpty() || this.conditions.getDimensions().contains(client.level.registryAccess().registryOrThrow(Registries.DIMENSION).getKey(client.level));
    }

    /**
     * @return Whether the current dimension sky effect is valid for this skybox
     */
    protected boolean checkWorlds() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        FabricSkyBoxesClient.getLogger().debug(this.conditions.getWorlds());
        return this.conditions.getWorlds().isEmpty() || this.conditions.getWorlds().contains(client.level.dimension().location());
    }

    /*
    	Check if an effect that should prevent skybox from showing
     */
    protected boolean checkEffects() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);

        Camera camera = client.gameRenderer.getMainCamera();

        if (this.conditions.getEffects().isEmpty()) {
            // Vanilla checks
            boolean thickFog = client.level.effects().isFoggyAt(camera.getBlockPosition().getX(), camera.getBlockPosition().getY()) || client.gui.getBossOverlay().shouldCreateWorldFog();
            if (thickFog) {
                // Render skybox in thick fog, enabled by default
                return this.properties.isRenderInThickFog();
            }

            FogType cameraSubmersionType = camera.getFluidInCamera();
            if (cameraSubmersionType == FogType.POWDER_SNOW || cameraSubmersionType == FogType.LAVA)
                return false;

            return !(camera.getEntity() instanceof LivingEntity livingEntity) || (!livingEntity.hasEffect(MobEffects.BLINDNESS) && !livingEntity.hasEffect(MobEffects.DARKNESS));

        } else {
            if (camera.getEntity() instanceof LivingEntity livingEntity) {
                return this.conditions.getEffects().stream().noneMatch(identifier -> client.level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).get(identifier) != null && livingEntity.hasEffect(client.level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).get(identifier)));
            }
        }
        return true;
    }

    /**
     * @return Whether the current x values are valid for this skybox.
     */
    protected boolean checkXRanges() {
        double playerX = Objects.requireNonNull(Minecraft.getInstance().player).getX();
        return Utils.checkRanges(playerX, this.conditions.getXRanges());
    }

    /**
     * @return Whether the current y values are valid for this skybox.
     */
    protected boolean checkYRanges() {
        double playerY = Objects.requireNonNull(Minecraft.getInstance().player).getY();
        return Utils.checkRanges(playerY, this.conditions.getYRanges());
    }

    /**
     * @return Whether the current z values are valid for this skybox.
     */
    protected boolean checkZRanges() {
        double playerZ = Objects.requireNonNull(Minecraft.getInstance().player).getZ();
        return Utils.checkRanges(playerZ, this.conditions.getZRanges());
    }

    /**
     * @return Whether the current loop is valid for this skybox.
     */
    protected boolean checkLoop() {
        if (!this.conditions.getLoop().getRanges().isEmpty() && this.conditions.getLoop().getDays() > 0) {
            double currentTime = Objects.requireNonNull(Minecraft.getInstance().level).getDayTime() - this.properties.getFade().getStartFadeIn();
            while (currentTime < 0) {
                currentTime += 24000 * this.conditions.getLoop().getDays();
            }

            double currentDay = (currentTime / 24000D) % this.conditions.getLoop().getDays();

            return Utils.checkRanges(currentDay, this.conditions.getLoop().getRanges());
        }
        return true;
    }

    /**
     * @return Whether the current weather is valid for this skybox.
     */
    protected boolean checkWeather() {
        ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        Biome.Precipitation precipitation = world.getBiome(player.blockPosition()).value().getPrecipitationAt(player.blockPosition());
        if (this.conditions.getWeathers().size() > 0) {
            if (this.conditions.getWeathers().contains(Weather.THUNDER) && world.isThundering()) {
                return true;
            }
            if (this.conditions.getWeathers().contains(Weather.SNOW) && world.isRaining() && precipitation == Biome.Precipitation.SNOW) {
                return true;
            }
            if (this.conditions.getWeathers().contains(Weather.RAIN) && world.isRaining() && !world.isThundering()) {
                return true;
            }
            return this.conditions.getWeathers().contains(Weather.CLEAR) && !world.isRaining();
        } else {
            return true;
        }
    }

    public abstract SkyboxType<? extends AbstractSkybox> getType();

    public void renderDecorations(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f matrix4f, float tickDelta, BufferBuilder bufferBuilder, float alpha) {
        RenderSystem.enableBlend();
        Vector3f rotationStatic = this.decorations.getRotation().getStatic();
        Vector3f rotationAxis = this.decorations.getRotation().getAxis();
        ClientLevel world = Minecraft.getInstance().level;
        assert world != null;

        // Custom Blender
        this.decorations.getBlend().applyBlendFunc(alpha);
        matrices.pushPose();

        // axis rotation
        matrices.mulPose(Axis.XP.rotationDegrees(rotationAxis.x()));
        matrices.mulPose(Axis.YP.rotationDegrees(rotationAxis.y()));
        matrices.mulPose(Axis.ZP.rotationDegrees(rotationAxis.z()));

        // Vanilla rotation
        //matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
        // Iris Compat
        //matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(IrisCompat.getSunPathRotation()));
        //matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(world.getSkyAngle(tickDelta) * 360.0F * this.decorations.getRotation().getRotationSpeed()));

        // Custom rotation
        double timeRotationX = this.decorations.getRotation().getRotationSpeedX() != 0F ? this.decorations.getRotation().getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedX()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedX()), 1))) : 0D;
        double timeRotationY = this.decorations.getRotation().getRotationSpeedY() != 0F ? this.decorations.getRotation().getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedY()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedY()), 1))) : 0D;
        double timeRotationZ = this.decorations.getRotation().getRotationSpeedZ() != 0F ? this.decorations.getRotation().getSkyboxRotation() ? 360D * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedZ()), 1) : 360D * world.dimensionType().timeOfDay((long) (24000 * Mth.positiveModulo(world.getDayTime() / (24000.0D / this.decorations.getRotation().getRotationSpeedZ()), 1))) : 0D;
        matrices.mulPose(Axis.XP.rotationDegrees((float) timeRotationX));
        matrices.mulPose(Axis.YP.rotationDegrees((float) timeRotationY));
        matrices.mulPose(Axis.ZP.rotationDegrees((float) timeRotationZ));

        // axis rotation
        matrices.mulPose(Axis.ZN.rotationDegrees(rotationAxis.z()));
        matrices.mulPose(Axis.YN.rotationDegrees(rotationAxis.y()));
        matrices.mulPose(Axis.XN.rotationDegrees(rotationAxis.x()));

        // static rotation
        matrices.mulPose(Axis.XP.rotationDegrees(rotationStatic.x()));
        matrices.mulPose(Axis.YP.rotationDegrees(rotationStatic.y()));
        matrices.mulPose(Axis.ZP.rotationDegrees(rotationStatic.z()));

        Pose matrix4f2 = matrices.last();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        // Sun
        if (this.decorations.isSunEnabled()) {
            RenderSystem.setShaderTexture(0, this.decorations.getSunTexture());
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f2.pose(), -30.0F, 100.0F, -30.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), 30.0F, 100.0F, -30.0F).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), 30.0F, 100.0F, 30.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), -30.0F, 100.0F, 30.0F).uv(0.0F, 1.0F).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
        }
        // Moon
        if (this.decorations.isMoonEnabled()) {
            RenderSystem.setShaderTexture(0, this.decorations.getMoonTexture());
            int moonPhase = world.getMoonPhase();
            int xCoord = moonPhase % 4;
            int yCoord = moonPhase / 4 % 2;
            float startX = xCoord / 4.0F;
            float startY = yCoord / 2.0F;
            float endX = (xCoord + 1) / 4.0F;
            float endY = (yCoord + 1) / 2.0F;
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f2.pose(), -20.0F, -100.0F, 20.0F).uv(endX, endY).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), 20.0F, -100.0F, 20.0F).uv(startX, endY).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), 20.0F, -100.0F, -20.0F).uv(startX, startY).endVertex();
            bufferBuilder.vertex(matrix4f2.pose(), -20.0F, -100.0F, -20.0F).uv(endX, startY).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
        }
        // Stars
        if (this.decorations.isStarsEnabled()) {
            float i = 1.0F - world.getRainLevel(tickDelta);
            float brightness = world.getStarBrightness(tickDelta) * i;
            if (brightness > 0.0F) {
                RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
                FogRenderer.setupNoFog();
                worldRendererAccess.getStarsBuffer().bind();
                worldRendererAccess.getStarsBuffer().drawWithShader(matrices.last().pose(), matrix4f, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }
        }
        matrices.popPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public Decorations getDecorations() {
        return this.decorations;
    }

    @Override
    public Properties getProperties() {
        return this.properties; // Properties.ofSkybox(this);
    }

    @Override
    public Conditions getConditions() {
        return this.conditions; // Conditions.ofSkybox(this);
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }

    @Override
    public int getPriority() {
        return this.properties.getPriority();
    }

    @Override
    public boolean isActive() {
        return this.getAlpha() > Constants.MINIMUM_ALPHA;
    }

    @Override
    public boolean isActiveLater() {
        final float oldAlpha = this.alpha;
        if (this.updateAlpha() > Constants.MINIMUM_ALPHA) {
            this.alpha = oldAlpha;
            return true;
        }
        return false;
    }
}
