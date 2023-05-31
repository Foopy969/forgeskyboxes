package com.foopy.forgeskyboxes.mixin.skybox;

import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.SkyboxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

@Mixin(LevelRenderer.class)
public abstract class SkyboxRenderMixin {

    /**
     * Contains the logic for when skyboxes should be rendered.
     */
    @Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();
        FabricSkyBoxesClient.getLogger().debug("ran mixin " + skyboxManager.getActiveSkyboxes().isEmpty());
        if (skyboxManager.isEnabled() && !skyboxManager.getActiveSkyboxes().isEmpty()) {
            FabricSkyBoxesClient.getLogger().debug("actually ran mixin");
            runnable.run();
            skyboxManager.renderSkyboxes((WorldRendererAccess) this, matrices, matrix4f, tickDelta, camera, bl);
            ci.cancel();
        }
    }
}
