package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    public static final Category CATEGORY = new Category("Singleplayer", new ItemStack(Items.NETHER_STAR));

    @Override
    public void onInitialize() {
        LOG.info("Initializing litematica printer");

        // Standard Modules
        Modules.get().add(new SpectatorModule());
        Modules.get().add(new GodMode());
        Modules.get().add(new Fly());
        Modules.get().add(new FastBridgeMacro());
        Modules.get().add(new CoinFlip());
        Modules.get().add(new CoinFlipSniper());
        Modules.get().add(new AntiAFK());
        Modules.get().add(new MentionAlert());
        Modules.get().add(new ChestStealer());
        Modules.get().add(new ChestDeposit());
        Modules.get().add(new FishCounter());
        Modules.get().add(new MovementMacro());
        Modules.get().add(new Printer());
        
        // Swapping MovementMacro for FarmReach
        Modules.get().add(new FarmReach()); 
        
        Modules.get().add(new Phase());
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
}
