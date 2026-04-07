package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class FarmReach extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("reach-range")
        .description("How far you can reach blocks/crops.")
        .defaultValue(5.0)
        .min(0)
        .sliderMax(10.0) 
        .build()
    );

    public FarmReach() {
        super(Addon.CATEGORY, "farm-reach", "Extends your reach for farming and block interaction.");
    }

    // Since getReachDistance is missing, we use this to tell the game 
    // the reach range. Note: If this still fails, your version requires a Mixin.
    public float getReach() {
        return isActive() ? range.get().floatValue() : 4.5f;
    }
}
