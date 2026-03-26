package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kkllffaa.meteor_litematica_printer.SpectatorModule;
import com.kkllffaa.meteor_litematica_printer.BowSlayer;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Spectator Plus");

    public static final Category CATEGORY = new Category("Custom");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Spectator Plus Addon...");

        Modules.get().add(new SpectatorModule());
        Modules.get().add(new BowSlayer());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }
}
