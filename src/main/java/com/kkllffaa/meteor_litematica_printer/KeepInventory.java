package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;

public class KeepInventory extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> restoreOnDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("restore-on-disable")
        .description("Restore keepInventory to false when disabling module.")
        .defaultValue(false)
        .build()
    );

    public KeepInventory() {
        super(Addon.CATEGORY, "keep-inventory", "Forces keepInventory gamerule in singleplayer.");
    }

    @Override
    public void onActivate() {
        setKeepInventory(true);
    }

    @Override
    public void onDeactivate() {
        if (restoreOnDisable.get()) setKeepInventory(false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Keep it enforced in case anything changes it.
        setKeepInventory(true);
    }

    private void setKeepInventory(boolean value) {
        if (mc.player == null) return;

        var server = mc.getSingleplayerServer();
        if (server == null) return; // silently ignore outside singleplayer

        server.execute(() -> {
            var level = server.overworld();
            if (level == null) return;

            var gameRules = level.getGameRules();
            gameRules.getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY).set(value, server);
        });

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal(value ? "§akeepInventory enabled." : "§ekeepInventory disabled."),
                false
            );
        }
    }
}
