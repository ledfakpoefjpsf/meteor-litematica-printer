package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Addon {
    public static void init() {
        // Get the module registry
        Modules modules = MeteorClient.getInstance().modules;

        // Register modules
        modules.add(new CreativeSurvivalModule());
        modules.add(new SpectatorModule());
    }
}
