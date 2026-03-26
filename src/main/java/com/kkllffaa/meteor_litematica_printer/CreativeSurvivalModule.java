package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import org.lwjgl.glfw.GLFW;

public class CreativeSurvivalModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Item> itemToSpawn = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to spawn.")
        .defaultValue(Items.DIAMOND)
        .build()
    );

    private final Setting<KeyBinding> spawnItemKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("spawn-key")
        .description("Key to spawn the item.")
        .defaultValue(new KeyBinding("Spawn Item", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "CreativeSurvival"))
        .build()
    );

    private final Setting<KeyBinding> enchantKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("enchant-key")
        .description("Key to enchant held item.")
        .defaultValue(new KeyBinding("Enchant Item", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, "CreativeSurvival"))
        .build()
    );

    public CreativeSurvivalModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.World, "creative-survival", "Spawn items and enchant in survival like creative.");
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        // Spawn item
        if (spawnItemKey.get().wasPressed()) {
            ItemStack stack = new ItemStack(itemToSpawn.get(), 64);
            if (player.getInventory().insertStack(stack)) {
                ChatUtils.info("Spawned 64 " + itemToSpawn.get().getName().getString());
            }
        }

        // Enchant held item
        if (enchantKey.get().wasPressed()) {
            ItemStack stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                EnchantmentHelper.setLevel(EnchantmentHelper.get(stack).keySet().iterator().next(), stack, 5);
                ChatUtils.info("Enchanted " + stack.getName().getString());
            }
        }
    }
}
