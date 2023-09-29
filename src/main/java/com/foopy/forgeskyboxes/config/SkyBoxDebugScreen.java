package com.foopy.forgeskyboxes.config;

import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.SkyboxManager;
import com.foopy.forgeskyboxes.api.skyboxes.FSBSkybox;
import com.foopy.forgeskyboxes.api.skyboxes.Skybox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import java.util.Map;

public class SkyBoxDebugScreen extends Screen {
    public SkyBoxDebugScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.onHudRender(context, delta);
    }
    
    public void onHudRender(GuiGraphics drawContext, float tickDelta) {
        if (FabricSkyBoxesClient.config().generalSettings.debugHud || Minecraft.getInstance().screen == this) {
            int yPadding = 2;
            for (Map.Entry<ResourceLocation, Skybox> ResourceLocationSkyboxEntry : SkyboxManager.getInstance().getSkyboxMap().entrySet()) {
                Skybox activeSkybox = ResourceLocationSkyboxEntry.getValue();
                if (activeSkybox instanceof FSBSkybox fsbSkybox && fsbSkybox.isActive()) {
                    drawContext.drawString(Minecraft.getInstance().font, ResourceLocationSkyboxEntry.getKey() + " " + activeSkybox.getPriority() + " " + fsbSkybox.getAlpha(), 2, yPadding, 0xffffffff, false);
                    yPadding += 14;
                }
            }
        }
    }
}
