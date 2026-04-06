package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

    private String lastTitle = "";
    private boolean done = false;

    public ChestDeposit() {
        super(Addon.CATEGORY, "chest-deposit", "Instantly deposits selected items into any chest you open.");
    }

    @Override
    public void onActivate() {
        lastTitle = "";
        done = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            lastTitle = "";
            done = false;
            return;
        }

        String title = screen.getTitle().getString();
        if (!title.equals(lastTitle)) {
            lastTitle = title;
            done = false;
        }

        if (done) return;
        done = true;

        var slots = screen.getMenu().slots;
        int chestSize = slots.size() - 36;

        // Scan player inventory slots only
        for (int i = chestSize; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (!stack.isEmpty() && shouldDeposit(stack)) {
                mc.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    i, 0,
                    ClickType.QUICK_MOVE,
                    mc.player
                );
            }
        }

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aDeposit done!"), false
            );
        }
    }

    private boolean shouldDeposit(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String idStr = id.toString();
        for (String item : itemsSetting.get()) {
            if (item.trim().equalsIgnoreCase(idStr)) return true;
        }
        return false;
    }
}
