package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SurvivalGive extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> itemSetting = sgGeneral.add(new StringSetting.Builder()
        .name("item")
        .description("Item ID to give (e.g. minecraft:diamond_sword)")
        .defaultValue("minecraft:diamond")
        .build()
    );

    private final Setting<Integer> countSetting = sgGeneral.add(new IntSetting.Builder()
        .name("count")
        .description("How many to give")
        .defaultValue(1)
        .min(1)
        .sliderMax(64)
        .build()
    );

    public SurvivalGive() {
        super(Categories.Player, "survival-give", "Give yourself any item in survival without enabling cheats.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        ResourceLocation id = ResourceLocation.tryParse(itemSetting.get());

        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            mc.player.displayClientMessage(Component.literal("§cInvalid item ID: " + itemSetting.get()), false);
            toggle();
            return;
        }

        Item item = BuiltInRegistries.ITEM.get(id).orElseThrow().value();
        ItemStack stack = new ItemStack(item, countSetting.get());

        boolean inserted = mc.player.getInventory().add(stack);

        if (inserted) {
            mc.player.displayClientMessage(Component.literal("§aGave " + countSetting.get() + "x " + id), false);
        } else {
            mc.player.displayClientMessage(Component.literal("§eInventory full!"), false);
        }

        toggle();
    }
}
