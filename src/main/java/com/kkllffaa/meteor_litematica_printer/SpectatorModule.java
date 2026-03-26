package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.world.entity.player.Player;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> playerName = sgGeneral.add(new StringSetting.Builder()
            .name("player-name")
            .description("The name of the player you want to watch.")
            .defaultValue("")
            .build()
    );

    public SpectatorModule() {
        super(Addon.CATEGORY, "spectator-plus", "Watch another player's perspective.");
    }

    @Override
    public void onActivate() {
        if (mc.level == null || mc.player == null) {
            this.toggle();
            return;
        }

        Player target = null;
        // 1.21.4 Fix: Iterate through players using the correct level method
        for (Player player : mc.level.players()) {
            // FIX: Use .name() instead of .getName()
            if (player.getGameProfile().getName().equalsIgnoreCase(playerName.get())) {
                target = player;
                break;
            }
        }

        if (target != null) {
            mc.setCameraEntity(target);
            // FIX: Use .name() for the message as well
            info("Now spectating: " + target.getGameProfile().getName());
        } else {
            error("Player '" + playerName.get() + "' not found nearby!");
            this.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            info("Camera reset to your perspective.");
        }
    }
}
