package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class MentionAlert extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> soundSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("sound")
        .description("Play a sound when mentioned")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> flashSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("flash-title")
        .description("Flash the Minecraft window title when mentioned")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> extraKeywordsSetting = sgGeneral.add(new StringSetting.Builder()
        .name("extra-keywords")
        .description("Extra words to alert on, comma separated e.g. trade,buy,sell")
        .defaultValue("")
        .build()
    );

    private boolean handling = false;

    public MentionAlert() {
        super(Addon.CATEGORY, "mention-alert", "Alerts you when your name is mentioned in chat.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (mc.player == null) return;
        if (handling) return; // prevent infinite loop

        String msg = event.getMessage().getString();

        // Ignore our own alert messages
        if (msg.contains("[MentionAlert]")) return;

        String playerName = mc.player.getName().getString().toLowerCase();
        boolean mentioned = msg.toLowerCase().contains(playerName);

        if (!mentioned && !extraKeywordsSetting.get().isEmpty()) {
            for (String keyword : extraKeywordsSetting.get().split(",")) {
                if (msg.toLowerCase().contains(keyword.trim().toLowerCase())) {
                    mentioned = true;
                    break;
                }
            }
        }

        if (!mentioned) return;

        handling = true;

        mc.player.displayClientMessage(
            Component.literal("§c[MentionAlert] §fYou were mentioned: §e" + msg), false
        );

        if (soundSetting.get() && mc.level != null) {
            mc.level.playLocalSound(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.MASTER,
                1.0f, 2.0f, false
            );
        }

        if (flashSetting.get()) {
            for (int i = 0; i < 5; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        Thread.sleep(index * 500L);
                        mc.execute(() -> {
                            if (mc.getWindow() != null) {
                                mc.getWindow().setTitle("*** YOU WERE MENTIONED ***");
                            }
                        });
                        Thread.sleep(250);
                        mc.execute(() -> {
                            if (mc.getWindow() != null) {
                                mc.getWindow().setTitle("Minecraft 1.21.4");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        handling = false;
    }
}
