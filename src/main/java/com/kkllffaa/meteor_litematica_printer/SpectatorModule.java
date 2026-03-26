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

        for (Player player : mc.level.players()) {
            if (player.getName().getString().equalsIgnoreCase(playerName.get())) {
                target = player;
                break;
            }
        }

        if (target != null) {
            mc.setCameraEntity(target);
            info("Now spectating: " + target.getName().getString());
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
