package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.render.RenderTooltipEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.GuiGraphics;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public SpectatorModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.Render, "spectator-module", "Show tooltip on armor in spectator mode.");
    }

    @EventHandler
    private void onRenderTooltip(RenderTooltipEvent event) {
        GuiGraphics graphics = event.graphics;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty()) {
                graphics.drawItem(armor, event.x, event.y);
                graphics.renderTooltip(event.textRenderer(), armor.getTooltipLines(player, null), event.x, event.y, null);
            }
        }
    }
}
