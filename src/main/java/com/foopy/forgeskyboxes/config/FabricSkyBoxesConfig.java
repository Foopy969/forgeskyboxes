package com.foopy.forgeskyboxes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.SkyboxManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class FabricSkyBoxesConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public final GeneralSettings generalSettings = new GeneralSettings();
    private final KeyBindingImpl keyBinding = new KeyBindingImpl();
    private File file;

    public static FabricSkyBoxesConfig load(File file) {
        FabricSkyBoxesConfig config;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, FabricSkyBoxesConfig.class);
            } catch (Exception e) {
                FabricSkyBoxesClient.getLogger().error("Could not parse config, falling back to defaults!", e);
                config = new FabricSkyBoxesConfig();
            }
        } else {
            config = new FabricSkyBoxesConfig();
        }
        config.file = file;
        config.save();

        return config;
    }

    public KeyBindingImpl getKeyBinding() {
        return this.keyBinding;
    }

    public void save() {
        File dir = this.file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
        }
    }

    public static class GeneralSettings {
        public boolean enable = true;
        public int unexpectedTransitionDuration = 20;
        public boolean keepVanillaBehaviour = true;

        public boolean debugMode = false;
        public boolean debugHud = false;
    }

    public static class KeyBindingImpl {

        public final KeyMapping toggleFabricSkyBoxes;
        public final KeyMapping toggleSkyboxDebugHud;

        public KeyBindingImpl() {
            this.toggleFabricSkyBoxes = new KeyMapping("key.forgeskyboxes.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.forgeskyboxes");
            this.toggleSkyboxDebugHud = new KeyMapping("key.forgeskyboxes.toggle.debug_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F12, "category.forgeskyboxes");
        }

        @SubscribeEvent
        public void onEndTick(ClientTickEvent event) {
            if (event.phase == Phase.START) return;
            while (this.toggleFabricSkyBoxes.consumeClick()) {
                FabricSkyBoxesClient.config().generalSettings.enable = !FabricSkyBoxesClient.config().generalSettings.enable;
                FabricSkyBoxesClient.config().save();
                SkyboxManager.getInstance().setEnabled(FabricSkyBoxesClient.config().generalSettings.enable);

                LocalPlayer player = Minecraft.getInstance().player;
                assert player != null;
                if (SkyboxManager.getInstance().isEnabled()) {
                    player.sendSystemMessage(Component.translatable("forgeskyboxes.message.enabled"));
                } else {
                    player.sendSystemMessage(Component.translatable("forgeskyboxes.message.disabled"));
                }
            }
            while (this.toggleSkyboxDebugHud.consumeClick()) {
                FabricSkyBoxesClient.config().generalSettings.debugHud = !FabricSkyBoxesClient.config().generalSettings.debugHud;
                FabricSkyBoxesClient.config().save();
            }
        }
    }
}
