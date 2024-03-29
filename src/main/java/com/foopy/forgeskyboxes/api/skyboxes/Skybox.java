package com.foopy.forgeskyboxes.api.skyboxes;

import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;


public interface Skybox {

    /**
     * For purposes on which order the skyboxes will be rendered.
     * The default priority will be set to 0.
     *
     * @return The priority of the skybox.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * The main render method for a skybox.
     * Override this if you are creating a skybox from this one.
     * This will be process if {@link Skybox#isActive()}
     *
     * @param worldRendererAccess Access to the worldRenderer as skyboxes often require it.
     * @param matrices            The current PoseStack.
     * @param tickDelta           The current tick delta.
     * @param camera              The player camera.
     * @param thickFog            Is using thick fog.
     */
    void render(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog);


    /**
     * The main thread for a skybox
     * Override this if you need to process conditions for the skybox.
     * This will be process regardless the state of {@link Skybox#isActive()}
     *
     * @param ClientLevel The client's world
     */
    void tick(ClientLevel ClientLevel);

    /**
     * Gets the state of the skybox.
     *
     * @return State of the skybox.
     */
    boolean isActive();
}
