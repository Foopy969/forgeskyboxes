package com.foopy.forgeskyboxes.api.skyboxes;

import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;

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
     *
     * @param worldRendererAccess Access to the worldRenderer as skyboxes often require it.
     * @param matrices            The current PoseStack.
     * @param tickDelta           The current tick delta.
     * @param camera              The player camera.
     * @param thickFog            Is using thick fog.
     */
    void render(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog);

    /**
     * Gets the state of the skybox.
     *
     * @return State of the skybox.
     */
    boolean isActive();

    /**
     * Whether the skybox will be active in the next frame.
     *
     * @return State of skybox of the next frame.
     */
    boolean isActiveLater();
}
