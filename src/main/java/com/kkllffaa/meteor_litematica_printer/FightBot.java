package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.commands.arguments.EntityAnchorArgument;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FightBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entitiesSetting = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select which mobs to attack")
        .onlyMobs(true)
        .build()
    );

    private final Setting<Double> rangeSetting = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Attack range in blocks")
        .defaultValue(4.0)
        .min(1.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Integer> attackDelaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("attack-delay-ticks")
        .description("Ticks between attacks")
        .defaultValue(10)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> lookAtTarget = sgGeneral.add(new BoolSetting.Builder()
        .name("look-at-target")
        .description("Look at the target before attacking")
        .defaultValue(true)
        .build()
    );

    private int tickCounter = 0;

    public FightBot() {
        super(Addon.CATEGORY, "fight-bot", "Automatically attacks mobs in range.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        tickCounter++;
        if (tickCounter < attackDelaySetting.get()) return;
        tickCounter = 0;

        double range = rangeSetting.get();
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.isAlive()) continue;
            if (living instanceof Player) continue;
            if (mc.player.distanceTo(living) > range) continue;
            if (!entitiesSetting.get().contains(living.getType())) continue;
            targets.add(living);
        }

        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        LivingEntity target = targets.get(0);

        if (lookAtTarget.get()) {
            mc.player.lookAt(
                EntityAnchorArgument.Anchor.EYES,
                target.position().add(0, target.getBbHeight() / 2, 0)
            );
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
