package com.foopy.forgeskyboxes.mixin.skybox;

import com.foopy.forgeskyboxes.SkyboxManager;
import com.foopy.forgeskyboxes.api.skyboxes.FSBSkybox;
import com.foopy.forgeskyboxes.api.skyboxes.Skybox;
import com.foopy.forgeskyboxes.util.Constants;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogColorMixin {

    @Shadow
    private static float fogRed;

    @Shadow
    private static float fogBlue;

    @Shadow
    private static float fogGreen;

    /**
     * Checks if we should change the fog color to whatever the skybox set it to, and sets it.
     */
    @Inject(method = "setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/FogRenderer;biomeChangedTime:J", ordinal = 6))
    private static void modifyColors(Camera camera, float tickDelta, ClientLevel world, int i, float f, CallbackInfo ci) {
        Skybox skybox = SkyboxManager.getInstance().getCurrentSkybox();
        if (skybox instanceof FSBSkybox fsbSkybox && fsbSkybox.getAlpha() > Constants.MINIMUM_ALPHA && fsbSkybox.getProperties().isChangeFog()) {
            fogRed = fsbSkybox.getProperties().getFogColors().getRed();
            fogBlue = fsbSkybox.getProperties().getFogColors().getBlue();
            fogGreen = fsbSkybox.getProperties().getFogColors().getGreen();
        }
    }
}
