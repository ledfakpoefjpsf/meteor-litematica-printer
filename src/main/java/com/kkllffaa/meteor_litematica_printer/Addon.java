package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Meteor Litematica Printer");
    
    // THIS LINE IS CRITICAL: It fixes the Printer.java error you got!
    public static final Category CATEGORY = new Category("Printer", Items.ORANGE_CONCRETE.getDefaultStack());

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Litematica Printer with Spectator Plus");

        // Registers the original printer
        Modules.get().add(new Printer());
        
        // Registers your new spectator module
        Modules.get().add(new SpectatorModule());
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }
}
