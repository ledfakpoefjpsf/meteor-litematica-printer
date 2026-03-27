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
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoinFlipSniper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> minAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("min-amount")
        .description("Minimum amount to snipe e.g. $10K, $1M")
        .defaultValue("$10K")
        .build()
    );

    private final Setting<String> maxAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("max-amount")
        .description("Maximum amount to snipe e.g. $1M, $10M")
        .defaultValue("$1M")
        .build()
    );

    private final Setting<Boolean> gcMode = sgGeneral.add(new BoolSetting.Builder()
        .name("gc-mode")
        .description("Snipe GC coinflips instead of $ coinflips")
        .defaultValue(false)
        .build()
    );

    private static final Pattern CF_PATTERN = Pattern.compile(
        "\\(/cf\\).*?(\\w+) made a.*?Coinflip.*?for: (.+)"
    );

    private String pendingTarget = null;
    private boolean waitingForConfirm = false;

    public CoinFlipSniper() {
        super(Addon.CATEGORY, "coinflip-sniper", "Auto-snipes coinflips in the /cf menu.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (mc.player == null) return;

        String msg = event.getMessage().getString();
        if (!msg.contains("(/cf)") || !msg.contains("made a")) return;

        Matcher matcher = CF_PATTERN.matcher(msg);
        if (!matcher.find()) return;

        String playerName = matcher.group(1).trim();
        String amountStr = matcher.group(2).trim();

        // Skip our own flips
        if (playerName.equalsIgnoreCase(mc.player.getName().getString())) return;

        // Check GC vs $ mode
        boolean isGC = !amountStr.startsWith("$");
        if (isGC != gcMode.get()) return;

        long amount = parseAmount(amountStr);
        long min = parseAmount(minAmountSetting.get());
        long max = parseAmount(maxAmountSetting.get());

        if (amount < min || amount > max) return;

        // Store target and open /cf menu
        pendingTarget = playerName;
        waitingForConfirm = false;
        mc.player.displayClientMessage(Component.literal(
            "§aSniping §e" + playerName + "§a's flip for §e" + amountStr
        ), false);

        // Small delay then open the menu
        new Thread(() -> {
            try {
                Thread.sleep(300);
                mc.execute(() -> mc.getConnection().sendCommand("cf"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundOpenScreenPacket packet)) return;
        String title = packet.getTitle().getString();

        if (title.equals("Active Coinflips") && pendingTarget != null) {
            // Wait a tick then scan for the target's head
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    mc.execute(() -> clickTargetHead());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        if (title.equals("Confirm") && waitingForConfirm) {
            // Wait a tick then click confirm
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    mc.execute(() -> clickConfirm());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void clickTargetHead() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        if (pendingTarget == null) return;

        var menu = screen.getMenu();
        var slots = menu.slots;

        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (stack.isEmpty()) continue;

            // Check tooltip for player name
            List<Component> tooltip = stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                net.minecraft.world.item.TooltipFlag.NORMAL
            );

            for (Component line : tooltip) {
                if (line.getString().contains(pendingTarget)) {
                    // Click this slot
                    mc.gameMode.handleInventoryMouseClick(
                        menu.containerId, i, 0, 
                        net.minecraft.world.inventory.ClickType.PICKUP, 
                        mc.player
                    );
                    waitingForConfirm = true;
                    return;
                }
            }
        }

        mc.player.displayClientMessage(Component.literal(
            "§cCouldn't find §e" + pendingTarget + "§c in the coinflip menu!"
        ), false);
        pendingTarget = null;
    }

    private void clickConfirm() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        var menu = screen.getMenu();
        var slots = menu.slots;

        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (stack.isEmpty()) continue;

            List<Component> tooltip = stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                net.minecraft.world.item.TooltipFlag.NORMAL
            );

            for (Component line : tooltip) {
                if (line.getString().equalsIgnoreCase("Confirm")) {
                    mc.gameMode.handleInventoryMouseClick(
                        menu.containerId, i, 0,
                        net.minecraft.world.inventory.ClickType.PICKUP,
                        mc.player
                    );
                    mc.player.displayClientMessage(Component.literal(
                        "§aCoinflip accepted!"
                    ), false);
                    pendingTarget = null;
                    waitingForConfirm = false;
                    return;
                }
            }
        }
    }

    private long parseAmount(String input) {
        if (input == null || input.isEmpty()) return 0;
        String s = input.trim().replace(",", "").replace("$", "").toUpperCase();
        try {
            if (s.endsWith("B")) return (long)(Double.parseDouble(s.replace("B", "")) * 1_000_000_000);
            if (s.endsWith("M")) return (long)(Double.parseDouble(s.replace("M", "")) * 1_000_000);
            if (s.endsWith("K")) return (long)(Double.parseDouble(s.replace("K", "")) * 1_000);
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
