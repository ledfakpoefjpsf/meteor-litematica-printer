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

    /**
     * This is the built-in Meteor method for modifying reach.
     * It overrides the vanilla reach distance automatically.
     */
    @Override
    public double getReachDistance() {
        return isActive() ? range.get() : super.getReachDistance();
    }
}
