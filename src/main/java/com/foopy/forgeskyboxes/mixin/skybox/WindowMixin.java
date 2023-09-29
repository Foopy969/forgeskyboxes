package com.foopy.forgeskyboxes.mixin.skybox;

import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void postWindowHints(WindowEventHandler eventHandler,ScreenManager monitorTracker, DisplayData settings, @Nullable String videoMode, String title, CallbackInfo ci) {
        if (FabricSkyBoxesClient.config().generalSettings.debugMode) {
            int maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
            FabricSkyBoxesClient.getLogger().info("Max Texture Size: {}x{}", maxTextureSize, maxTextureSize);
            FabricSkyBoxesClient.getLogger().info("Extension EXT_blend_func_extended supported: {}", GLFW.glfwExtensionSupported("EXT_blend_func_extended"));
            FabricSkyBoxesClient.getLogger().info("Extension GL_KHR_blend_equation_advanced supported: {}", GLFW.glfwExtensionSupported("GL_KHR_blend_equation_advanced"));
            FabricSkyBoxesClient.getLogger().info("Extension GL_KHR_blend_equation_advanced_coherent supported: {}", GLFW.glfwExtensionSupported("GL_KHR_blend_equation_advanced_coherent"));
        }
    }
}
