package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;

import java.util.List;

public class CoinFlip extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> amountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("amount")
        .description("Amount to coinflip e.g. $10K, $1M, 2GC")
        .defaultValue("$1K")
        .build()
    );

    public enum CurrencyType { MONEY, GC, EXP }

    private final Setting<CurrencyType> currencySetting = sgGeneral.add(new EnumSetting.Builder<CurrencyType>()
        .name("currency")
        .description("Which currency to use for the coinflip")
        .defaultValue(CurrencyType.MONEY)
        .build()
    );

    private boolean waitingForCurrencyMenu = false;
    private int winStreak = 0;
    private int totalWins = 0;
    private int totalLosses = 0;

    public CoinFlip() {
        super(Addon.CATEGORY, "coinflip", "Sends /cf in chat and tracks your win streak.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        waitingForCurrencyMenu = true;
        mc.getConnection().sendCommand("cf " + amountSetting.get());
        toggle();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundOpenScreenPacket packet)) return;

        String title = packet.getTitle().getString();

        // Debug - print every GUI title to chat
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§eGUI opened: §f" + title), false
            );
        }

        if (!waitingForCurrencyMenu) return;
        if (!title.contains("Wager")) return;

        new Thread(() -> {
            try {
                Thread.sleep(200);
                mc.execute(() -> clickCurrency());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clickCurrency() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        String target = switch (currencySetting.get()) {
            case MONEY -> "Wager Money";
            case GC -> "Wager GC";
            case EXP -> "Wager Experience";
        };

        var slots = screen.getMenu().slots;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (stack.isEmpty()) continue;

            List<Component> tooltip = stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                net.minecraft.world.item.TooltipFlag.NORMAL
            );

            for (Component line : tooltip) {
                if (line.getString().equalsIgnoreCase(target)) {
                    mc.gameMode.handleInventoryMouseClick(
                        screen.getMenu().containerId, i, 0,
                        ClickType.PICKUP,
                        mc.player
                    );
                    waitingForCurrencyMenu = false;
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                            Component.literal("§aCoinflip created with " + target + "!"), false
                        );
                    }
                    return;
                }
            }
        }

        // Debug - couldn't find the slot
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§cCouldn't find slot for: " + target), false
            );
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString();

        if (msg.contains("You have won the coinflip")) {
            totalWins++;
            winStreak++;
            printStats(true);
        } else if (msg.contains("You have lost the coinflip")) {
            totalLosses++;
            winStreak = 0;
            printStats(false);
        }
    }

    private void printStats(boolean won) {
        if (mc.player == null) return;

        double streakProb = Math.pow(0.5, winStreak) * 100;
        int total = totalWins + totalLosses;
        double winRate = total > 0 ? (totalWins * 100.0 / total) : 0;

        mc.player.displayClientMessage(Component.literal(
            (won ? "§aWon! " : "§cLost! ") +
            "§fStreak: §e" + winStreak +
            " §f| Next flip: §e50%" +
            " §f| Streak prob: §e" + String.format("%.2f", streakProb) + "%" +
            " §f| Win rate: §e" + String.format("%.1f", winRate) + "%" +
            " §f(" + totalWins + "W/" + totalLosses + "L)"
        ), false);
    }
}
