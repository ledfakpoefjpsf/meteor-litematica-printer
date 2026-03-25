package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.world.entity.player.Player;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> playerName = sgGeneral.add(new StringSetting.Builder()
            .name("player-name")
            .description("Type the name of the player to watch.")
            .defaultValue("")
            .build()
    );

    public SpectatorModule() {
        super(Categories.Player, "spectator-plus", "Watch another player.");
    }

    @Override
    public void onActivate() {
        // In Official Java Mappings, we use 'level' instead of 'world'
        if (mc.level == null || mc.player == null) return;

        Player target = null;
        for (Player player : mc.level.players()) {
            // FIX: Using .getScoreboardName() or .getName().getString() is safer in 1.21.4
            if (player.getGameProfile().getName().equalsIgnoreCase(playerName.get())) {
                target = player;
                break;
            }
        }

        if (target != null) {
            mc.setCameraEntity(target);
            info("Spectating: " + target.getGameProfile().getName());
        } else {
            error("Player not found nearby!");
            this.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            info("Camera reset.");
        }
    }
}
