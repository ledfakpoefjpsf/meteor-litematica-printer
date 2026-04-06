package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ChestScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ChestStealer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks between each item steal")
        .defaultValue(2)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private boolean stealing = false;
    private int tickDelay = 0;
    private int currentSlot = 0;

    public ChestStealer() {
        super(Addon.CATEGORY, "chest-stealer", "Automatically takes all items from a chest when opened.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof ChestScreen) {
            stealing = true;
            currentSlot = 0;
            tickDelay = 0;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!stealing) return;
        if (!(mc.screen instanceof ChestScreen screen)) {
            stealing = false;
            return;
        }

        tickDelay++;
        if (tickDelay < delaySetting.get()) return;
        tickDelay = 0;

        var slots = screen.getMenu().slots;

        // Only steal from chest slots, not player inventory
        int chestSize = slots.size() - 36;

        while (currentSlot < chestSize) {
            ItemStack stack = slots.get(currentSlot).getItem();
            if (!stack.isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    currentSlot, 0,
                    ClickType.QUICK_MOVE,
                    mc.player
                );
                currentSlot++;
                return;
            }
            currentSlot++;
        }

        // Done stealing
        stealing = false;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aChest stealer finished!"), false
            );
        }
    }
}
