package com.foopy.forgeskyboxes.api;

import com.google.gson.JsonObject;
import com.foopy.forgeskyboxes.SkyboxManager;
import com.foopy.forgeskyboxes.api.skyboxes.Skybox;
import com.foopy.forgeskyboxes.util.JsonObjectWrapper;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface FabricSkyBoxesApi {

    /**
     * @since API v0.0
     */
    static FabricSkyBoxesApi getInstance() {
        return SkyboxManager.getInstance();
    }

    /**
     * Gets the version of this API, This is incremented when changes are implemented
     * without breaking API. Mods can use this to check if given API functionality
     * is available on the current version of installed FabricSkyBoxes.
     *
     * @return The current version of the API
     */
    int getApiVersion();

    /**
     * Gets the state of the FabricSkyBoxes mod.
     *
     * @return Whether FabricSkyBoxes is enabled.
     */
    boolean isEnabled();

    /**
     * Allows mods to set the state of the FabricSkyBoxes.
     *
     * @param enabled State of the FabricSkyBoxes.
     */
    void setEnabled(boolean enabled);

    /**
     * Allows mods to add new skyboxes at runtime.
     *
     * @param ResourceLocation ResourceLocation for skybox.
     * @param skybox     Skybox implementation.
     */
    void addSkybox(ResourceLocation ResourceLocation, Skybox skybox);

    /**
     * Allows mods to add new skyboxes with a {@link JsonObject} at runtime.
     * This method applies {@link SkyboxManager#parseSkyboxJson(ResourceLocation, JsonObjectWrapper)}
     * serialization and adds the skybox with {@link #addSkybox(ResourceLocation, Skybox)}
     *
     * @param ResourceLocation ResourceLocation for skybox.
     * @param jsonObject Json Object.
     */
    void addSkybox(ResourceLocation ResourceLocation, JsonObject jsonObject);

    /**
     * Allows mods to add new permanent skyboxes at runtime.
     *
     * @param ResourceLocation ResourceLocation for skybox.
     * @param skybox     Skybox implementation.
     */
    void addPermanentSkybox(ResourceLocation ResourceLocation, Skybox skybox);

    /**
     * Clears all non-permanent skyboxes.
     */
    void clearSkyboxes();

    /**
     * Gets the current skybox that is being rendered.
     *
     * @return Current skybox being render, returns null of nothing is being rendered.
     */
    Skybox getCurrentSkybox();

    /**
     * Gets a list of active skyboxes.
     *
     * @return Current list of active skyboxes.
     */
    List<Skybox> getActiveSkyboxes();
}
