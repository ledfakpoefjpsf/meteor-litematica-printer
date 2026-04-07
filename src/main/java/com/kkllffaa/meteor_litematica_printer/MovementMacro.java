package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MovementMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> recordingSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("recording")
        .description("Manual recording toggle")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> loopSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("loop")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> repeatsSetting = sgGeneral.add(new IntSetting.Builder()
        .name("repeats")
        .defaultValue(1)
        .min(1)
        .sliderMax(100)
        .build()
    );

    // 🔥 NEW KEYBINDS
    private final Setting<Keybind> recordToggleKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-toggle-key")
        .description("Press to toggle recording on/off")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Keybind> holdRecordKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("hold-record-key")
        .description("Hold to record movements")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> useHoldMode = sgGeneral.add(new BoolSetting.Builder()
        .name("use-hold-to-record")
        .defaultValue(false)
        .build()
    );

    private static class Frame {
        boolean forward, backward, left, right, jump, sneak, sprint;
        float yaw, pitch;

        Frame(boolean forward, boolean backward, boolean left, boolean right,
              boolean jump, boolean sneak, boolean sprint, float yaw, float pitch) {
            this.forward = forward;
            this.backward = backward;
            this.left = left;
            this.right = right;
            this.jump = jump;
            this.sneak = sneak;
            this.sprint = sprint;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private final List<Frame> recorded = new ArrayList<>();
    private boolean wasRecording = false;
    private boolean playing = false;
    private int playIndex = 0;
    private int repeatCount = 0;

    public MovementMacro() {
        super(Addon.CATEGORY, "movement-macro", "Record and replay movement.");
    }

    @Override
    public void onActivate() {
        playing = false;
        playIndex = 0;
        repeatCount = 0;
        wasRecording = false;
    }

    @Override
    public void onDeactivate() {
        playing = false;
        restoreInputs();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        boolean isRecording;

        // HOLD MODE
        if (useHoldMode.get()) {
            isRecording = holdRecordKey.get().isDown();
        }
        // TOGGLE MODE
        else {
            if (recordToggleKey.get().isPressed()) {
                boolean newState = !recordingSetting.get();
                recordingSetting.set(newState);

                mc.player.displayClientMessage(
                    Component.literal(newState ? "§c[Macro] Recording ON"
                                               : "§a[Macro] Recording OFF"),
                    false
                );
            }

            isRecording = recordingSetting.get();
        }

        // START RECORDING
        if (isRecording && !wasRecording) {
            recorded.clear();
            playing = false;
            wasRecording = true;

            mc.player.displayClientMessage(
                Component.literal("§c[Macro] Recording started!"), false
            );
        }

        // STOP RECORDING → PLAYBACK
        if (!isRecording && wasRecording) {
            wasRecording = false;

            if (recorded.isEmpty()) {
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] Nothing recorded!"), false
                );
                return;
            }

            mc.player.displayClientMessage(
                Component.literal("§a[Macro] Recorded " + recorded.size() + " ticks! Playing..."), false
            );

            playing = true;
            playIndex = 0;
            repeatCount = 0;
        }

        // RECORDING
        if (isRecording) {
            var o = mc.options;

            recorded.add(new Frame(
                o.forwardKey.isPressed(),
                o.backKey.isPressed(),
                o.leftKey.isPressed(),
                o.rightKey.isPressed(),
                o.jumpKey.isPressed(),
                o.sneakKey.isPressed(),
                o.sprintKey.isPressed(),
                mc.player.getYRot(),
                mc.player.getXRot()
            ));
            return;
        }

        // PLAYBACK
        if (!playing || recorded.isEmpty()) return;

        Frame f = recorded.get(playIndex);
        applyFrame(mc.player, f);
        playIndex++;

        if (playIndex >= recorded.size()) {
            playIndex = 0;
            repeatCount++;

            if (!loopSetting.get() && repeatCount >= repeatsSetting.get()) {
                playing = false;
                restoreInputs();

                mc.player.displayClientMessage(
                    Component.literal("§a[Macro] Playback finished!"), false
                );
            }
        }
    }

    private void applyFrame(LocalPlayer player, Frame f) {
        var o = mc.options;

        o.forwardKey.setDown(f.forward);
        o.backKey.setDown(f.backward);
        o.leftKey.setDown(f.left);
        o.rightKey.setDown(f.right);

        o.jumpKey.setDown(f.jump);
        o.sneakKey.setDown(f.sneak);
        o.sprintKey.setDown(f.sprint);

        player.setYRot(f.yaw);
        player.setXRot(f.pitch);
    }

    private void restoreInputs() {
        if (mc.options == null) return;
        var o = mc.options;

        o.forwardKey.setDown(false);
        o.backKey.setDown(false);
        o.leftKey.setDown(false);
        o.rightKey.setDown(false);

        o.jumpKey.setDown(false);
        o.sneakKey.setDown(false);
        o.sprintKey.setDown(false);
    }
}
