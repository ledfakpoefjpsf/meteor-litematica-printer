package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphics;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> playerName = sgGeneral.add(new StringSetting.Builder()
            .name("player-name")
            .description("Target to spy on.")
            .defaultValue("")
            .build()
    );

    public SpectatorModule() {
        super(Addon.CATEGORY, "spectator-plus", "Spy on player gear and tooltips.");
    }

    // This method handles drawing the items on your screen
    public void onRender(GuiGraphics graphics) {
        if (mc.level == null || !isActive()) return;

        Player target = null;
        for (Player player : mc.level.players()) {
            if (player.getName().getString().equalsIgnoreCase(playerName.get())) {
                target = player;
                break;
            }
        }

        if (target != null) {
            int x = mc.getWindow().getGuiScaledWidth() / 2 + 100;
            int y = mc.getWindow().getGuiScaledHeight() / 2 - 50;

            // Draw Main Hand
            drawItem(graphics, target.getMainHandItem(), x, y, "Hand");
            // Draw Armor pieces
            int offset = 20;
            for (ItemStack armor : target.getArmorSlots()) {
                drawItem(graphics, armor, x, y + offset, "");
                offset += 20;
            }
        }
    }

    private void drawItem(GuiGraphics graphics, ItemStack stack, int x, int y, String label) {
        if (stack.isEmpty()) return;
        
        // Render the actual item icon
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(mc.font, stack, x, y);

        // Check if your mouse is hovering over this icon
        double mouseX = mc.mouseHandler.x() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getWidth();
        double mouseY = mc.mouseHandler.y() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight();

        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
            // This is the "Magic": It shows the full Skyblock tooltip (Tier, Enchants, etc.)
            graphics.renderTooltip(mc.font, stack, (int)mouseX, (int)mouseY);
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.setCameraEntity(mc.player);
    }
}
