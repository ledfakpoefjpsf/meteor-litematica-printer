package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SurvivalGive extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> itemSetting = sgGeneral.add(new StringSetting.Builder()
        .name("item")
        .description("Item ID to spawn (e.g. minecraft:diamond_sword)")
        .defaultValue("minecraft:diamond")
        .build()
    );

    private final Setting<Integer> countSetting = sgGeneral.add(new IntSetting.Builder()
        .name("count")
        .description("How many to spawn")
        .defaultValue(1)
        .min(1)
        .sliderMax(64)
        .build()
    );

    public SurvivalGive() {
        super(Categories.Player, "survival-give", "Spawn items in singleplayer by writing to server inventory.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        var server = mc.getSingleplayerServer();
        if (server == null) {
            mc.player.displayClientMessage(Component.literal("§cSingleplayer only."), false);
            toggle();
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(itemSetting.get());
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            mc.player.displayClientMessage(Component.literal("§cInvalid item ID: " + itemSetting.get()), false);
            toggle();
            return;
        }

        int count = countSetting.get();
        var uuid = mc.player.getUUID();

        server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(uuid);
            if (sp == null) return;

            var itemHolder = BuiltInRegistries.ITEM.get(id).orElse(null);
            if (itemHolder == null) return;

            var stack = new ItemStack(itemHolder, count);

            boolean added = sp.getInventory().add(stack);
            if (!added) sp.drop(stack, false);
            sp.containerMenu.broadcastChanges();
        });

        mc.player.displayClientMessage(Component.literal("§aSpawned " + count + "x " + id), false);
        toggle();
    }
}
