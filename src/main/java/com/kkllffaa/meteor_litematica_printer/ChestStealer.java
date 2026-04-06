package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    @Override
    public void onActivate() {
        stealing = false;
        currentSlot = 0;
        tickDelay = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            if (stealing) {
                stealing = false;
                currentSlot = 0;
            }
            return;
        }

        String title = screen.getTitle().getString().toLowerCase();
        if (!title.contains("chest") && !title.contains("container") && !title.contains("shulker")) return;

        if (!stealing) {
            stealing = true;
            currentSlot = 0;
            tickDelay = 0;
        }

        tickDelay++;
        if (tickDelay < delaySetting.get()) return;
        tickDelay = 0;

        var slots = screen.getMenu().slots;
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

        stealing = false;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aChest stealer finished!"), false
            );
        }
    }
}
