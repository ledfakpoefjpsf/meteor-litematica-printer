package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Fly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal fly speed.")
        .defaultValue(0.8)
        .min(0.05)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Up/down fly speed.")
        .defaultValue(0.5)
        .min(0.05)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Boolean> antiKick = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-kick")
        .description("Applies tiny downward motion to reduce idle-fly kicks.")
        .defaultValue(true)
        .build()
    );

    public Fly() {
        super(Addon.CATEGORY, "fly", "Simple client fly movement.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().mayfly = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            // Keep mayfly true only if player is creative/spectator
            boolean keepMayfly = mc.player.isCreative() || mc.player.isSpectator();
            mc.player.getAbilities().mayfly = keepMayfly;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        p.setOnGround(false);
        p.fallDistance = 0.0f;

        double h = horizontalSpeed.get();
        double v = verticalSpeed.get();

        Vec3d vel = p.getVelocity();
        double vy = 0.0;

        if (mc.options.jumpKey.isPressed()) vy += v;
        if (mc.options.sneakKey.isPressed()) vy -= v;

        double forward = 0.0;
        double strafe = 0.0;
        if (mc.options.forwardKey.isPressed()) forward += 1.0;
        if (mc.options.backKey.isPressed()) forward -= 1.0;
        if (mc.options.leftKey.isPressed()) strafe += 1.0;
        if (mc.options.rightKey.isPressed()) strafe -= 1.0;

        if (forward != 0.0 || strafe != 0.0) {
            double yaw = Math.toRadians(p.getYaw());
            double sin = Math.sin(yaw);
            double cos = Math.cos(yaw);

            double mx = (forward * cos - strafe * sin) * h;
            double mz = (forward * sin + strafe * cos) * h;
            p.setVelocity(mx, vy, mz);
        } else {
            double idleY = (antiKick.get() && !mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) ? -0.04 : 0.0;
            p.setVelocity(0.0, vy + idleY, 0.0);
        }
    }
}
