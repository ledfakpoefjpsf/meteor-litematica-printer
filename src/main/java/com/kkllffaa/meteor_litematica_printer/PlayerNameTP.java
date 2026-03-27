package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PlayerJumpTP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> targetName = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("Name of the player to jump to.")
        .defaultValue("")
        .build()
    );

    private final Setting<Double> stepSize = sgGeneral.add(new DoubleSetting.Builder()
        .name("step-size")
        .description("Blocks moved per packet.")
        .defaultValue(10.0)
        .min(0.1)
        .sliderMax(25.0)
        .build()
    );

    public PlayerJumpTP() {
        super(Addon.CATEGORY, "player-jump-tp", "Moves to a player's position in steps.");
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
            error("Player not found in loaded players.");
            toggle();
            return;
        }

        Vec3 start = mc.player.position();
        Vec3 end = target.position();
        double distance = start.distanceTo(end);

        int steps = (int) Math.ceil(distance / stepSize.get());
        steps = Math.max(1, steps);

        for (int i = 1; i <= steps; i++) {
            double lerp = (double) i / steps;
            double nextX = start.x + (end.x - start.x) * lerp;
            double nextY = start.y + (end.y - start.y) * lerp;
            double nextZ = start.z + (end.z - start.z) * lerp;

            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
                nextX, nextY, nextZ, mc.player.onGround(), false
            ));
        }

        mc.player.setPos(end.x, end.y, end.z);
        info("Attempted jump to " + name + " in " + steps + " steps.");
        toggle();
    }
}
