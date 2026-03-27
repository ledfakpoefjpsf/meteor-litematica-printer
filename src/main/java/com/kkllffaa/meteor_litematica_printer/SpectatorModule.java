package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public SpectatorModule() {
        super(Categories.Render, "spectator-module", "Show armor items on HUD in spectator mode.");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.isSpectator()) return;

        int x = 5;
        int y = 5;
        for (int i = 3; i >= 0; i--) {
            ItemStack armor = player.getInventory().armor.get(i);
            if (!armor.isEmpty()) {
                mc.gameRenderer.itemRenderer.renderGuiItem(armor, x, y);
                x += 20;
            }
        }
    }
}
