package com.foopy.forgeskyboxes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import com.foopy.forgeskyboxes.api.FabricSkyBoxesApi;
import com.foopy.forgeskyboxes.api.skyboxes.FSBSkybox;
import com.foopy.forgeskyboxes.api.skyboxes.Skybox;
import com.foopy.forgeskyboxes.mixin.skybox.WorldRendererAccess;
import com.foopy.forgeskyboxes.skyboxes.AbstractSkybox;
import com.foopy.forgeskyboxes.skyboxes.SkyboxType;
import com.foopy.forgeskyboxes.util.JsonObjectWrapper;
import com.foopy.forgeskyboxes.util.object.internal.Metadata;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SkyboxManager implements FabricSkyBoxesApi {
    private static final SkyboxManager INSTANCE = new SkyboxManager();
    private final Map<ResourceLocation, Skybox> skyboxMap = new Object2ObjectLinkedOpenHashMap<>();
    /**
     * Stores a list of permanent skyboxes
     *
     * @see #addPermanentSkybox(ResourceLocation, Skybox)
     */
    private final Map<ResourceLocation, Skybox> permanentSkyboxMap = new Object2ObjectLinkedOpenHashMap<>();
    private final List<Skybox> activeSkyboxes = new LinkedList<>();
    private final Predicate<? super Skybox> renderPredicate = (skybox) -> !this.activeSkyboxes.contains(skybox) && skybox.isActive();
    private Skybox currentSkybox = null;
    private boolean enabled = true;
    private float totalAlpha = 0f;

    public static AbstractSkybox parseSkyboxJson(ResourceLocation id, JsonObjectWrapper objectWrapper) {
        AbstractSkybox skybox;
        Metadata metadata;

        try {
            metadata = Metadata.CODEC.decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        } catch (RuntimeException e) {
            FabricSkyBoxesClient.getLogger().warn("Skipping invalid skybox " + id.toString(), e);
            FabricSkyBoxesClient.getLogger().warn(objectWrapper.toString());
            return null;
        }

        SkyboxType<? extends AbstractSkybox> type = SkyboxType.REGISTRY.get().getValue(metadata.getType());
        Preconditions.checkNotNull(type, "Unknown skybox type: " + metadata.getType().getPath().replace('_', '-'));
        if (metadata.getSchemaVersion() == 1) {
            Preconditions.checkArgument(type.isLegacySupported(), "Unsupported schema version '1' for skybox type " + type.getName());
            FabricSkyBoxesClient.getLogger().debug("Using legacy deserializer for skybox " + id.toString());
            skybox = type.instantiate();
            //noinspection ConstantConditions
            type.getDeserializer().getDeserializer().accept(objectWrapper, skybox);
        } else {
            skybox = type.getCodec(metadata.getSchemaVersion()).decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        }
        return skybox;
    }

    public static SkyboxManager getInstance() {
        return INSTANCE;
    }

    public void addSkybox(ResourceLocation ResourceLocation, JsonObject jsonObject) {
        Skybox skybox = SkyboxManager.parseSkyboxJson(ResourceLocation, new JsonObjectWrapper(jsonObject));
        if (skybox != null) {
            this.addSkybox(ResourceLocation, skybox);
            this.sortSkybox();
        }
    }

    public void addSkybox(ResourceLocation ResourceLocation, Skybox skybox) {
        Preconditions.checkNotNull(ResourceLocation, "ResourceLocation was null");
        Preconditions.checkNotNull(skybox, "Skybox was null");
        this.skyboxMap.put(ResourceLocation, skybox);
        this.sortSkybox();
    }

    /**
     * Sorts skyboxes by ascending order with priority field. Skyboxes with
     * identical priority will not be re-ordered, this will largely come down to
     * the alphabetical order that Minecraft resources load in.
     * <p>
     * Minecraft's resource loading order example:
     * "fabricskyboxes:sky/overworld_sky1.json"
     * "fabricskyboxes:sky/overworld_sky10.json"
     * "fabricskyboxes:sky/overworld_sky11.json"
     * "fabricskyboxes:sky/overworld_sky2.json"
     */
    private void sortSkybox() {
        Map<ResourceLocation, Skybox> newSortedMap = this.skyboxMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingInt(Skybox::getPriority))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (skybox, skybox2) -> skybox, Object2ObjectLinkedOpenHashMap::new));
        this.skyboxMap.clear();
        this.skyboxMap.putAll(newSortedMap);
    }

    /**
     * Permanent skyboxes are never cleared after a resource reload. This is
     * useful when adding skyboxes through code as resource reload listeners
     * have no defined order of being called.
     *
     * @param skybox the skybox to be added to the list of permanent skyboxes
     */
    public void addPermanentSkybox(ResourceLocation ResourceLocation, Skybox skybox) {
        Preconditions.checkNotNull(ResourceLocation, "ResourceLocation was null");
        Preconditions.checkNotNull(skybox, "Skybox was null");
        this.permanentSkyboxMap.put(ResourceLocation, skybox);
    }

    @Internal
    public void clearSkyboxes() {
        this.skyboxMap.clear();
        this.activeSkyboxes.clear();
    }

    @Internal
    public float getTotalAlpha() {
        return this.totalAlpha;
    }

    @Internal
    public void renderSkyboxes(WorldRendererAccess worldRendererAccess, PoseStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog) {
        this.activeSkyboxes.forEach(skybox -> {
            this.currentSkybox = skybox;
            skybox.render(worldRendererAccess, matrices, matrix4f, tickDelta, camera, thickFog);
        });
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Skybox getCurrentSkybox() {
        return this.currentSkybox;
    }

    @Override
    public int getApiVersion() {
        return 0;
    }

    @Override
    public List<Skybox> getActiveSkyboxes() {
        return this.activeSkyboxes;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event) {
        if (event.phase == Phase.END) {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            if (Minecraft.getInstance().player == null) {
                return;
            }
            INSTANCE.onEndTick();
        }
    }

    public void onEndTick() {
        // Add the skyboxes to a activeSkyboxes container so that they can be ordered
        this.skyboxMap.values().stream().filter(this.renderPredicate).forEach(this.activeSkyboxes::add);
        this.permanentSkyboxMap.values().stream().filter(this.renderPredicate).forEach(this.activeSkyboxes::add);

        // Let's not sort by alpha value
        //this.activeSkyboxes.sort((skybox1, skybox2) -> skybox1 instanceof FSBSkybox fsbSkybox1 && skybox2 instanceof FSBSkybox fsbSkybox2 ? Float.compare(fsbSkybox1.getAlpha(), fsbSkybox2.getAlpha()) : 0);
        this.activeSkyboxes.sort((skybox1, skybox2) -> skybox1 instanceof FSBSkybox fsbSkybox1 && skybox2 instanceof FSBSkybox fsbSkybox2 ? Integer.compare(fsbSkybox1.getPriority(), fsbSkybox2.getPriority()) : 0);

        this.totalAlpha = (float) StreamSupport.stream(Iterables.concat(this.skyboxMap.values(), this.permanentSkyboxMap.values()).spliterator(), false).filter(FSBSkybox.class::isInstance).map(FSBSkybox.class::cast).mapToDouble(FSBSkybox::updateAlpha).sum();

        this.activeSkyboxes.removeIf(skybox -> !skybox.isActiveLater());
    }
}
