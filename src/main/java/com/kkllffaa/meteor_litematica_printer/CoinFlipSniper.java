package com.kkllffaa.meteor_litematica_printer;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
    private final Setting<Boolean> randomDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("random-delay")
        .description("Wait a random 3-10 seconds before running /cf. Turn off for no delay.")
        .defaultValue(true)
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
        if (playerName.equalsIgnoreCase(mc.player.getName().getString())) return;
        boolean isGC = !amountStr.startsWith("$");
        if (isGC != gcMode.get()) return;
        long amount = parseAmount(amountStr);
        long min = parseAmount(minAmountSetting.get());
        long max = parseAmount(maxAmountSetting.get());
        if (amount < min || amount > max) return;
        pendingTarget = playerName;
        waitingForConfirm = false;
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                "§aSniping §e" + playerName + "§a's flip for §e" + amountStr
            ), false);
        }
        new Thread(() -> {
            try {
                if (randomDelay.get()) {
                    long ms = ThreadLocalRandom.current().nextLong(3000, 10001);
                    Thread.sleep(ms);
                }
                mc.execute(() -> mc.getConnection().sendCommand("cf"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.screen == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        String title = screen.getTitle().getString();
        // Debug
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§eTicking GUI: §f" + title), false
            );
        }
        // Looking for target player head
        if (pendingTarget != null && !waitingForConfirm) {
            var slots = screen.getMenu().slots;
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getItem();
                if (stack.isEmpty()) continue;
                List<Component> tooltip = stack.getTooltipLines(
                    net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                    net.minecraft.world.item.TooltipFlag.NORMAL
                );
                for (Component line : tooltip) {
                    String lineText = line.getString();
                    // Debug every tooltip
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                            Component.literal("§7Slot " + i + ": §f" + lineText), false
                        );
                    }
                    if (lineText.contains(pendingTarget)) {
                        mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId, i, 0,
                            ClickType.PICKUP,
                            mc.player
                        );
                        waitingForConfirm = true;
                        if (mc.player != null) {
                            mc.player.displayClientMessage(Component.literal(
                                "§aClicked §e" + pendingTarget + "§a's head!"
                            ), false);
                        }
                        return;
                    }
                }
            }
        }
        // Looking for confirm button
        if (waitingForConfirm) {
            var slots = screen.getMenu().slots;
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getItem();
                if (stack.isEmpty()) continue;
                List<Component> tooltip = stack.getTooltipLines(
                    net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                    net.minecraft.world.item.TooltipFlag.NORMAL
                );
                for (Component line : tooltip) {
                    String lineText = line.getString();
                    if (lineText.equalsIgnoreCase("Confirm")) {
                        mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId, i, 0,
                            ClickType.PICKUP,
                            mc.player
                        );
                        if (mc.player != null) {
                            mc.player.displayClientMessage(Component.literal(
                                "§aCoinflip accepted!"
                            ), false);
                        }
                        pendingTarget = null;
                        waitingForConfirm = false;
                        return;
                    }
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
