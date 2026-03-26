package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Spectator Plus");
    
    // This is your "Shelf"
    public static final Category CATEGORY = new Category("Custom");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Spectator Plus Addon...");

        // FIX: This line registers the category so Meteor knows it exists!
        Modules.registerCategory(CATEGORY);
        
        // Now we can safely add the module to that category
        Modules.get().add(new SpectatorModule());
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }
}
