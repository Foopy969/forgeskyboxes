package com.foopy.forgeskyboxes.mixin.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import com.foopy.forgeskyboxes.SkyboxManager;
import com.foopy.forgeskyboxes.util.Utils;
import com.foopy.forgeskyboxes.util.object.FogRGBA;
import com.foopy.forgeskyboxes.util.object.RGBA;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogColorMixin {

    @Unique
    private static float density;

    @Unique
    private static boolean modifyDensity;

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
        FogRGBA fogColor = Utils.alphaBlendFogColors(SkyboxManager.getInstance().getActiveSkyboxes(), new RGBA(fogRed, fogGreen, fogBlue));
        if (SkyboxManager.getInstance().isEnabled() && fogColor != null) {
            fogRed = fogColor.getRed();
            fogGreen = fogColor.getGreen();
            fogBlue = fogColor.getBlue();
            density = fogColor.getDensity();
            modifyDensity = true;
        } else {
            modifyDensity = false;
        }
    }

    @Redirect(method = "levelFogColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogColor(FFF)V"))
    private static void redirectSetShaderFogColor(float red, float green, float blue) {
        if (modifyDensity) {
            RenderSystem.setShaderFogColor(red, green, blue, density);
        } else {
            RenderSystem.setShaderFogColor(red, green, blue);
        }
    }
}
