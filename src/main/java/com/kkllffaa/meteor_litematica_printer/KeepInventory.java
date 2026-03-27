package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.GameRules;

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
        setKeepInventory(true); // enforce silently
    }

    private void setKeepInventory(boolean value) {
        if (mc.getSingleplayerServer() == null) return;

        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            var level = server.overworld();
            if (level == null) return;

            var rule = level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
            if (rule.get() != value) {
                rule.set(value, server);
            }
        });
    }
}
