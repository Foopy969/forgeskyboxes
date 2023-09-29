package com.foopy.forgeskyboxes.mixin.skybox;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.VertexBuffer;

@Mixin(LevelRenderer.class)
public interface WorldRendererAccess {
    @Deprecated
    @Accessor("SUN_LOCATION")
    static ResourceLocation getSun() {
        throw new AssertionError();
    }

    @Deprecated
    @Accessor("MOON_LOCATION")
    static ResourceLocation getMoonPhases() {
        throw new AssertionError();
    }

    @Deprecated
    @Accessor("END_SKY_LOCATION")
    static ResourceLocation getEndSky() {
        throw new AssertionError();
    }

    @Accessor("skyBuffer")
    VertexBuffer getLightSkyBuffer();

    @Accessor("starBuffer")
    VertexBuffer getStarsBuffer();
    
    @Accessor("darkBuffer")
    VertexBuffer getDarkSkyBuffer();
}
