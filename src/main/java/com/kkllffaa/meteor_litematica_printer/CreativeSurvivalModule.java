package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.ModuleType;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputUtil;

public class CreativeSurvivalModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // -----------------------------
    // TOGGLE SETTINGS (checkboxes)
    // -----------------------------
    private final Setting<Boolean> flyEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("fly")
            .description("Enable flying")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> vanishEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("vanish")
            .description("Enable vanish mode")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> godmodeEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("godmode")
            .description("Enable godmode")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> instaBreakEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("insta-break")
            .description("Break blocks instantly")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> spawnItemsEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("spawn-items")
            .description("Spawn items automatically")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> overEnchantEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("over-enchant")
            .description("Over-enchant items in hand")
            .defaultValue(false)
            .build());

    // -----------------------------
    // ITEM SPAWN SETTINGS
    // -----------------------------
    private final Setting<Item> itemToSpawn = sgGeneral.add(new ItemSetting.Builder()
            .name("item-to-spawn")
            .description("Select any item/block to spawn.")
            .defaultValue(Items.DIAMOND)
            .build());

    private final Setting<Integer> spawnAmount = sgGeneral.add(new IntSetting.Builder()
            .name("spawn-amount")
            .description("How many items to spawn at once")
            .defaultValue(64)
            .min(1)
            .max(999)
            .sliderMax(64)
            .build());

    // -----------------------------
    // OPTIONAL KEYBINDS
    // -----------------------------
    private final Setting<KeybindSetting> spawnItemKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("spawn-item-key")
            .description("Key to spawn selected item")
            .defaultValue(new KeybindSetting(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, false))
            .build());

    private final Setting<KeybindSetting> enchantKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("enchant-key")
            .description("Key to over-enchant the item in hand")
            .defaultValue(new KeybindSetting(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, false))
            .build());

    public CreativeSurvivalModule() {
        super(ModuleType.Player, "creative-survival", "Ultimate creative powers in survival (Vanish, Godmode, Enchants, Fly, Spawn Items)");
    }

    @Override
    public void onTick() {
        // -----------------------------
        // FLY
        // -----------------------------
        if (flyEnabled.get()) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().allowFlying = true;
        } else {
            mc.player.getAbilities().flying = false;
        }

        // -----------------------------
        // INSTANT BREAK
        // -----------------------------
        if (instaBreakEnabled.get()) PlayerUtils.breakBlockInstant();

        // -----------------------------
        // SPAWN ITEMS
        // -----------------------------
        if (spawnItemsEnabled.get() || spawnItemKey.get().isPressed()) {
            Item selected = itemToSpawn.get();
            int amount = spawnAmount.get();
            if (selected != null) {
                for (int i = 0; i < amount; i++) {
                    mc.player.getInventory().insertStack(selected.getDefaultStack());
                }
            }
        }

        // -----------------------------
        // VANISH
        // -----------------------------
        mc.player.setInvisible(vanishEnabled.get());

        // -----------------------------
        // GODMODE
        // -----------------------------
        if (godmodeEnabled.get()) {
            mc.player.setHealth(mc.player.getMaxHealth());
            mc.player.getHungerManager().setFoodLevel(20);
        }

        // -----------------------------
        // OVER-ENCHANT
        // -----------------------------
        if (overEnchantEnabled.get() || enchantKey.get().wasPressed()) {
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty()) enchantItem(stack, 3000); // Sharpness 3000 example
        }
    }

    private void enchantItem(ItemStack stack, int level) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound enchants = new NbtCompound();
        enchants.putInt("minecraft:sharpness", level);
        nbt.put("Enchantments", enchants);
        stack.setNbt(nbt);
    }

    @Override
    public void onDeactivate() {
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
        mc.player.setInvisible(false);
    }
}
