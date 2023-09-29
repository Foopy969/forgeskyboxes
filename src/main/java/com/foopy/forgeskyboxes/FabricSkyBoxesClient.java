package com.foopy.forgeskyboxes;

import com.foopy.forgeskyboxes.config.FabricSkyBoxesConfig;
import com.foopy.forgeskyboxes.config.SkyBoxDebugScreen;
import com.foopy.forgeskyboxes.resource.SkyboxResourceListener;
import com.foopy.forgeskyboxes.skyboxes.LegacyDeserializer;
import com.foopy.forgeskyboxes.skyboxes.SkyboxType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FabricSkyBoxesClient.MODID)
public class FabricSkyBoxesClient {
    public static final String MODID = "forgeskyboxes";
    private static Logger LOGGER;
    private static FabricSkyBoxesConfig CONFIG;

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("ForgeSkyboxes");
        }
        return LOGGER;
    }

    public static FabricSkyBoxesConfig config() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    private static FabricSkyBoxesConfig loadConfig() {
        return FabricSkyBoxesConfig.load(FMLPaths.CONFIGDIR.get().resolve("forgeskyboxes-config.json").toFile());
    }

    public FabricSkyBoxesClient() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        SkyboxType.SKYBOX_TYPE.register(bus);
        LegacyDeserializer.DESERIALIZER.register(bus);

        bus.addListener(this::onInitializeClient);
        bus.addListener(this::registerBindings);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onInitializeClient(FMLClientSetupEvent event) {
        SkyboxType.initRegistry();
        SkyboxManager.getInstance().setEnabled(config().generalSettings.enable);

        ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        resourceManager.registerReloadListener(new SkyboxResourceListener());

        MinecraftForge.EVENT_BUS.register(SkyboxManager.getInstance());
        MinecraftForge.EVENT_BUS.register(config().getKeyBinding());
        //
        //KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgeskyboxes.toggle.debug_screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.forgeskyboxes"));
        SkyBoxDebugScreen screen = new SkyBoxDebugScreen(Component.literal("Skybox Debug Screen"));
        /*ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.setScreen(screen);
            }
        });*/
        MinecraftForge.EVENT_BUS.register(screen);
        Minecraft.getInstance().reloadResourcePacks();
    }

    public void registerBindings(RegisterKeyMappingsEvent event) {
        FabricSkyBoxesConfig.KeyBindingImpl keyMappings = config().getKeyBinding();

        event.register(keyMappings.toggleFabricSkyBoxes);
        event.register(keyMappings.toggleSkyboxDebugHud);
    }
}