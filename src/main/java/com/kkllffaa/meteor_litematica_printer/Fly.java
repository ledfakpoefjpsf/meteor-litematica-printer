package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;

public class Fly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Master speed multiplier for fly movement.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderMax(10.0)
        .build()
    );

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
            boolean keepMayfly = mc.player.isCreative() || mc.player.isSpectator();
            mc.player.getAbilities().mayfly = keepMayfly;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        LocalPlayer p = mc.player;
        if (p == null) return;

        p.setOnGround(false);
        p.fallDistance = 0.0f;

        double multiplier = speed.get();
        double h = horizontalSpeed.get() * multiplier;
        double v = verticalSpeed.get() * multiplier;

        double vy = 0.0;
        if (mc.options.keyJump.isDown()) vy += v;
        if (mc.options.keyShift.isDown()) vy -= v;

        double forward = 0.0;
        double strafe = 0.0;

        if (mc.options.keyUp.isDown()) forward += 1.0;     // W
        if (mc.options.keyDown.isDown()) forward -= 1.0;   // S
        if (mc.options.keyLeft.isDown()) strafe -= 1.0;    // A
        if (mc.options.keyRight.isDown()) strafe += 1.0;   // D

        if (forward != 0.0 || strafe != 0.0) {
            double yaw = Math.toRadians(p.getYRot());
            double sin = Math.sin(yaw);
            double cos = Math.cos(yaw);

            double mx = (forward * -sin + strafe * cos) * h;
            double mz = (forward *  cos + strafe * sin) * h;
            p.setDeltaMovement(mx, vy, mz);
        } else {
            double idleY = (antiKick.get() && !mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()) ? -0.04 : 0.0;
            p.setDeltaMovement(0.0, vy + idleY, 0.0);
        }
    }
}
