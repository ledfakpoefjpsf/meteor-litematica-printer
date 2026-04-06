package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class ChestDeposit extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> itemsSetting = sgGeneral.add(new StringListSetting.Builder()
        .name("items-to-deposit")
        .description("List of item IDs to deposit (e.g. minecraft:cod, minecraft:salmon)")
        .defaultValue(List.of(
            "minecraft:cod",
            "minecraft:salmon",
            "minecraft:tropical_fish",
            "minecraft:pufferfish",
            "minecraft:cooked_cod",
            "minecraft:cooked_salmon"
        ))
        .build()
    );

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks between each deposit")
        .defaultValue(2)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private boolean depositing = false;
    private int currentSlot = 0;
    private int tickDelay = 0;
    private String lastTitle = "";

    public ChestDeposit() {
        super(Addon.CATEGORY, "chest-deposit", "Auto deposits selected items into any chest you open.");
    }

    @Override
    public void onActivate() {
        depositing = false;
        currentSlot = 0;
        tickDelay = 0;
        lastTitle = "";
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            if (depositing) {
                depositing = false;
                currentSlot = 0;
            }
            return;
        }

        String title = screen.getTitle().getString();

        // Reset when a new chest is opened
        if (!title.equals(lastTitle)) {
            lastTitle = title;
            depositing = true;
            currentSlot = 0;
            tickDelay = 0;
        }

        if (!depositing) return;

        tickDelay++;
        if (tickDelay < delaySetting.get()) return;
        tickDelay = 0;

        var slots = screen.getMenu().slots;
        int chestSize = slots.size() - 36;

        // Scan player inventory (slots after chest slots)
        while (currentSlot < slots.size()) {
            // Only scan player inventory slots
            if (currentSlot < chestSize) {
                currentSlot++;
                continue;
            }

            ItemStack stack = slots.get(currentSlot).getItem();
            if (!stack.isEmpty() && shouldDeposit(stack)) {
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

        // Done depositing
        depositing = false;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aDeposit finished!"), false
            );
        }
    }

    private boolean shouldDeposit(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String idStr = id.toString();
        for (String item : itemsSetting.get()) {
            if (item.trim().equalsIgnoreCase(idStr)) return true;
        }
        return false;
    }
}
