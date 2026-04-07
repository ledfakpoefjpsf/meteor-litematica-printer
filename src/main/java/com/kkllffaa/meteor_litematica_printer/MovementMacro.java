package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind; // Added for keybind support
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MovementMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> recordingSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("recording")
        .description("Turn on to record, turn off to stop and save")
        .defaultValue(false)
        .build()
    );

    // --- Added Keybind Setting ---
    private final Setting<Keybind> recordKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-keybind")
        .description("The key used to toggle recording on/off.")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> loopSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("loop")
        .description("Loop the macro continuously")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> repeatsSetting = sgGeneral.add(new IntSetting.Builder()
        .name("repeats")
        .description("How many times to repeat if loop is off")
        .defaultValue(1)
        .min(1)
        .sliderMax(100)
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
    private boolean lastKeyStatus = false; // Prevents spamming the toggle while holding the key

    public MovementMacro() {
        super(Addon.CATEGORY, "movement-macro", "Record your movements and play them back on repeat.");
    }

    @Override
    public void onActivate() {
        playing = false;
        playIndex = 0;
        repeatCount = 0;
        wasRecording = false;
        lastKeyStatus = false;

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§e[Macro] §fPress your §aKeybind §for toggle §aRecording §fto start."), false
            );
        }
    }

    @Override
    public void onDeactivate() {
        playing = false;
        recordingSetting.set(false); // Reset recording checkbox if module is disabled
        restoreInputs();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // --- Keybind Logic ---
        boolean isKeyPressed = recordKey.get().isPressed();
        if (isKeyPressed && !lastKeyStatus) {
            // This flips the boolean setting when the key is pressed down
            recordingSetting.set(!recordingSetting.get()); 
        }
        lastKeyStatus = isKeyPressed;

        boolean isRecording = recordingSetting.get();

        if (isRecording && !wasRecording) {
            recorded.clear();
            playing = false;
            wasRecording = true;
            mc.player.displayClientMessage(
                Component.literal("§c[Macro] §fRecording started!"), false
            );
        }

        if (!isRecording && wasRecording) {
            wasRecording = false;

            if (recorded.isEmpty()) {
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] §fNothing recorded!"), false
                );
                return;
            }

            mc.player.displayClientMessage(
                Component.literal("§a[Macro] §fRecorded §e" + recorded.size() + " §fticks! Starting playback..."), false
            );

            playing = true;
            playIndex = 0;
            repeatCount = 0;
        }

        // RECORDING
        if (isRecording) {
            var options = mc.options;

            recorded.add(new Frame(
                options.keyUp.isDown(),
                options.keyDown.isDown(),
                options.keyLeft.isDown(),
                options.keyRight.isDown(),
                options.keyJump.isDown(),
                options.keyShift.isDown(),
                options.keySprint.isDown(),
                mc.player.getYRot(),
                mc.player.getXRot()
            ));
            return;
        }

        // PLAYBACK
        if (!playing || recorded.isEmpty()) return;

        Frame frame = recorded.get(playIndex);
        applyFrame(mc.player, frame);
        playIndex++;

        if (playIndex >= recorded.size()) {
            playIndex = 0;
            repeatCount++;

            if (!loopSetting.get() && repeatCount >= repeatsSetting.get()) {
                playing = false;
                restoreInputs();

                mc.player.displayClientMessage(
                    Component.literal("§a[Macro] §fPlayback finished!"), false
                );
            }
        }
    }

    private void applyFrame(LocalPlayer player, Frame frame) {
        var options = mc.options;

        options.keyUp.setDown(frame.forward);
        options.keyDown.setDown(frame.backward);
        options.keyLeft.setDown(frame.left);
        options.keyRight.setDown(frame.right);

        options.keyJump.setDown(frame.jump);
        options.keyShift.setDown(frame.sneak);
        options.keySprint.setDown(frame.sprint);

        player.setYRot(frame.yaw);
        player.setXRot(frame.pitch);
    }

    private void restoreInputs() {
        if (mc.options == null) return;

        var options = mc.options;

        options.keyUp.setDown(false);
        options.keyDown.setDown(false);
        options.keyLeft.setDown(false);
        options.keyRight.setDown(false);

        options.keyJump.setDown(false);
        options.keyShift.setDown(false);
        options.keySprint.setDown(false);
    }
}
