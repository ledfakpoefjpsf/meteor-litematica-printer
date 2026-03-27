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
        Modules.get().add(new SurvivalGive());
        Modules.get().add(new SpectatorModule());
        Modules.get().add(new GodMode());
        Modules.get().add(new Fly());
        Modules.get().add(new EnchantAny());
        Modules.get().add(new KeepInventory());
        Modules.get().add(new SingleplayerCommand());
        Modules.get().add(new FastBridgeMacro());
        Modules.get().add(new PlayerJumpTP());
        Modules.get().add(new CoinFlip());
        Modules.get().add(new CoinFlipSniper());
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
