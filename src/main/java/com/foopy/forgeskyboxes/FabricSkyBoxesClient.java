package com.foopy.forgeskyboxes;

import com.foopy.forgeskyboxes.resource.SkyboxResourceListener;
import com.foopy.forgeskyboxes.skyboxes.LegacyDeserializer;
import com.foopy.forgeskyboxes.skyboxes.SkyboxType;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(FabricSkyBoxesClient.MODID)
public class FabricSkyBoxesClient {
    public static final String MODID = "forgeskyboxes";
    private static Logger LOGGER;
    private static KeyMapping toggleFabricSkyBoxes;

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("ForgeSkyboxes");
        }
        return LOGGER;
    }

    public FabricSkyBoxesClient() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        SkyboxType.SKYBOX_TYPE.register(bus);
        LegacyDeserializer.DESERIALIZER.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SkyboxManager.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::onInitializeClient);
    }

    public void onInitializeClient() {
        SkyboxType.initRegistry();
        toggleFabricSkyBoxes = new KeyMapping("key.forgeskyboxes.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_0, "category.forgeskyboxes");
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new SkyboxResourceListener());
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent event) {
        if (event.phase == Phase.END) {
            SkyboxManager.getInstance();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            while (toggleFabricSkyBoxes.consumeClick()) {
                Minecraft client = Minecraft.getInstance();
                SkyboxManager.getInstance().setEnabled(!SkyboxManager.getInstance().isEnabled());
                Objects.requireNonNull(client.player);
                if (SkyboxManager.getInstance().isEnabled()) {
                    client.player.sendSystemMessage(Component.translatable("forgeskyboxes.message.enabled"));
                } else {
                    client.player.sendSystemMessage(Component.translatable("forgeskyboxes.message.disabled"));
                }
            }
        }
    }
}
