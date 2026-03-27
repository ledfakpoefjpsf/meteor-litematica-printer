package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;

public class PlayerNameTP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> targetName = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("Exact player name to snap to.")
        .defaultValue("")
        .build()
    );

    public PlayerNameTP() {
        super(Addon.CATEGORY, "player-name-tp", "Snap to a loaded player's location.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            toggle();
            return;
        }

        String name = targetName.get().trim();
        if (name.isEmpty()) {
            error("Enter a player name first.");
            toggle();
            return;
        }

        Player target = null;
        for (Player p : mc.level.players()) {
            if (p.getGameProfile().getName().equalsIgnoreCase(name)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            error("Player '" + name + "' not found (must be loaded client-side).");
            toggle();
            return;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        // Client-side snap
        mc.player.setPos(x, y, z);

        // Tell server new position (5-arg constructor for your mappings/version)
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
            x, y, z, mc.player.onGround(), false
        ));

        info("Attempted snap to " + name + ".");
        toggle();
    }
}
