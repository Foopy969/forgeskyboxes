package com.foopy.forgeskyboxes.mixin.skybox;

import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.SkyboxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.material.FogType;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyboxRenderMixin {

    @Invoker("doesMobEffectBlockSky")
    protected abstract boolean doesMobEffectBlockSky(Camera camera);

    /**
     * Contains the logic for when skyboxes should be rendered.
     */
    @Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();
        if (skyboxManager.isEnabled() && !skyboxManager.getActiveSkyboxes().isEmpty()) {
            runnable.run();
            FogType fogType = camera.getFluidInCamera();
            boolean renderSky = !FabricSkyBoxesClient.config().generalSettings.keepVanillaBehaviour || (!bl && fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && fogType != FogType.WATER && !this.doesMobEffectBlockSky(camera));
            if (renderSky) {
                skyboxManager.renderSkyboxes((WorldRendererAccess) this, matrices, matrix4f, tickDelta, camera, bl);
            }
            ci.cancel();
        }
    }
}
