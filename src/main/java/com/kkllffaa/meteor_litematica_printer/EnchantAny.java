package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EnchantAny extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> enchantSetting = sgGeneral.add(new StringSetting.Builder()
        .name("enchantment")
        .description("Enchantment ID, e.g. minecraft:sharpness")
        .defaultValue("minecraft:sharpness")
        .build()
    );

    private final Setting<Integer> levelSetting = sgGeneral.add(new IntSetting.Builder()
        .name("level")
        .description("Enchantment level to apply.")
        .defaultValue(10)
        .min(1)
        .sliderMax(255)
        .build()
    );

    public EnchantAny() {
        super(Addon.CATEGORY, "enchant-any", "Apply any enchant level to held item (singleplayer).");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        var server = mc.getSingleplayerServer();
        if (server == null) {
            mc.player.displayClientMessage(Component.literal("§eWorks in singleplayer world only."), false);
            toggle();
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(enchantSetting.get());
        if (id == null || !BuiltInRegistries.ENCHANTMENT.containsKey(id)) {
            mc.player.displayClientMessage(Component.literal("§cInvalid enchantment: " + enchantSetting.get()), false);
            toggle();
            return;
        }

        int level = levelSetting.get();
        var uuid = mc.player.getUUID();

        server.execute(() -> {
            var sp = server.getPlayerList().getPlayer(uuid);
            if (sp == null) return;

            var held = sp.getMainHandItem();
            if (held.isEmpty()) return;

            var enchHolder = BuiltInRegistries.ENCHANTMENT.get(id).orElse(null);
            if (enchHolder == null) return;

            held.enchant(enchHolder, level);
            sp.containerMenu.broadcastChanges();
        });

        mc.player.displayClientMessage(
            Component.literal("§aApplied " + enchantSetting.get() + " " + level + " to held item."),
            false
        );
        toggle();
    }
}
