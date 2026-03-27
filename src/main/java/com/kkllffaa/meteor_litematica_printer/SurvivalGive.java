package com.kkllffaa.meteorlitematicaprinter;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        Identifier id = Identifier.tryParse(itemSetting.get());

        if (id == null || !Registries.ITEM.containsId(id)) {
            mc.player.sendMessage(Text.literal("§cInvalid item ID: " + itemSetting.get()), false);
            toggle();
            return;
        }

        Item item = Registries.ITEM.get(id);
        ItemStack stack = new ItemStack(item, countSetting.get());

        boolean inserted = mc.player.getInventory().insertStack(stack);

        if (inserted) {
            mc.player.sendMessage(Text.literal("§aGave " + countSetting.get() + "x " + id), false);
        } else {
            mc.player.sendMessage(Text.literal("§eInventory full!"), false);
        }

        toggle(); // auto-disables after giving
    }
}
