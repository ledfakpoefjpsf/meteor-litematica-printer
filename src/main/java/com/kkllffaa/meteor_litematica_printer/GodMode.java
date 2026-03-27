package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GodMode extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> refillHunger = sgGeneral.add(new BoolSetting.Builder()
        .name("refill-hunger")
        .description("Keeps hunger and saturation full.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> extinguishFire = sgGeneral.add(new BoolSetting.Builder()
        .name("extinguish-fire")
        .description("Removes fire and reset freeze ticks.")
        .defaultValue(true)
        .build()
    );

    public GodMode() {
        super(Addon.CATEGORY, "god-mode", "Singleplayer invulnerability (Integrated Server only).");
    }

    @Override
    public void onActivate() {
        updateState(true);
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§aGodMode enabled."), false);
        }
    }

    @Override
    public void onDeactivate() {
        updateState(false);
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§cGodMode disabled."), false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        updateState(true);
    }

    private void updateState(boolean state) {
        if (mc.player == null || mc.getSingleplayerServer() == null) return;

        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();

        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp == null) return;

            if (sp.getAbilities().invulnerable != state) {
                sp.getAbilities().invulnerable = state;
                sp.onUpdateAbilities();
            }

            if (state) {
                sp.setHealth(sp.getMaxHealth());
                sp.setAirSupply(sp.getMaxAirSupply());

                if (refillHunger.get()) {
                    sp.getFoodData().setFoodLevel(20);
                    sp.getFoodData().setSaturation(20.0f);
                }

                if (extinguishFire.get()) {
                    sp.clearFire();
                    sp.setTicksFrozen(0);
                }
            }
        });
    }
}
