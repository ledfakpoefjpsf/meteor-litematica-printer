package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Meteor Litematica Printer");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Litematica Printer with Spectator Plus");

        // Keeps your Printer module working
        Modules.get().add(new Printer());
        
        // Adds your new Spectator module
        Modules.get().add(new SpectatorModule());
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }
}
