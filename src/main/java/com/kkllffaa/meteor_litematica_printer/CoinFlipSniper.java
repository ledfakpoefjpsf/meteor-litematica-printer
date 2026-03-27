package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.ClickType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoinFlipSniper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> minAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("min-amount")
        .description("Minimum amount to snipe (e.g. $10K, $1M).")
        .defaultValue("$10K")
        .build()
    );

    private final Setting<String> maxAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("max-amount")
        .description("Maximum amount to snipe (e.g. $1M, $10M).")
        .defaultValue("$1M")
        .build()
    );

    private final Setting<Boolean> gcMode = sgGeneral.add(new BoolSetting.Builder()
        .name("gc-mode")
        .description("Snipe GC coinflips instead of $ coinflips.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> randomDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("random-delay")
        .description("Random 3-10s delay before /cf. Off = no delay.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maxLosses = sgGeneral.add(new IntSetting.Builder()
        .name("max-losses")
        .description("Disable the module after this many losses in a row (wins reset the streak).")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<String> lossRegex = sgGeneral.add(new StringSetting.Builder()
        .name("loss-regex")
        .description("Regex for a loss line in chat (case-insensitive). Tune for your server.")
        .defaultValue("(?i).*(you lost|lost the coin|lost the flip).*")
        .build()
    );

    private final Setting<String> winRegex = sgGeneral.add(new StringSetting.Builder()
        .name("win-regex")
        .description("Regex for a win; resets the loss streak. Empty = never reset from chat.")
        .defaultValue("(?i).*(you won|won the coin|won the flip).*")
        .build()
    );

    private final Setting<Boolean> feedback = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Print short status lines in chat when sniping / clicking / disabled.")
        .defaultValue(true)
        .build()
    );

    private static final Pattern CF_PATTERN = Pattern.compile(
        "\\(/cf\\).*?(\\w+) made a.*?Coinflip.*?for: (.+)"
    );

    private String pendingTarget = null;
    private boolean waitingForConfirm = false;
    private int lossStreak = 0;

    public CoinFlipSniper() {
        super(Addon.CATEGORY, "coinflip-sniper", "Auto-snipes coinflips in the /cf menu.");
    }

    @Override
    public void onActivate() {
        lossStreak = 0;
    }

    @Override
    public void onDeactivate() {
        pendingTarget = null;
        waitingForConfirm = false;
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (mc.player == null) return;

        String msg = event.getMessage().getString();

        if (isActive()) {
            String win = winRegex.get();
            if (!win.isEmpty() && matchesRegex(win, msg)) {
                lossStreak = 0;
            }

            String loss = lossRegex.get();
            if (!loss.isEmpty() && matchesRegex(loss, msg)) {
                lossStreak++;
                int cap = maxLosses.get();
                if (lossStreak >= cap) {
                    lossStreak = 0;
                    mc.execute(() -> {
                        msgFeedback("§cCoinflip Sniper disabled after " + cap + " losses.");
                        disable();
                    });
                }
                return;
            }
        }

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

        msgFeedback("§aSniping §e" + playerName + "§a for §e" + amountStr);

        new Thread(() -> {
            try {
                if (randomDelay.get()) {
                    long ms = ThreadLocalRandom.current().nextLong(3000, 10001);
                    Thread.sleep(ms);
                }
                mc.execute(() -> {
                    if (mc.getConnection() != null) {
                        mc.getConnection().sendCommand("cf");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.screen == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        if (pendingTarget != null && !waitingForConfirm) {
            Integer slot = findSlotWithTooltipContaining(screen, pendingTarget);
            if (slot != null) {
                clickSlot(screen, slot);
                waitingForConfirm = true;
                msgFeedback("§aClicked §e" + pendingTarget + "§a.");
            }
            return;
        }

        if (waitingForConfirm) {
            Integer slot = findSlotWithTooltipContaining(screen, "Confirm");
            if (slot != null) {
                clickSlot(screen, slot);
                msgFeedback("§aCoinflip accepted.");
                pendingTarget = null;
                waitingForConfirm = false;
            }
        }
    }

    private void clickSlot(AbstractContainerScreen<?> screen, int slotIndex) {
        mc.gameMode.handleInventoryMouseClick(
            screen.getMenu().containerId,
            slotIndex,
            0,
            ClickType.PICKUP,
            mc.player
        );
    }

    private Integer findSlotWithTooltipContaining(AbstractContainerScreen<?> screen, String needle) {
        var slots = screen.getMenu().slots;
        boolean confirm = needle.equalsIgnoreCase("Confirm");

        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (stack.isEmpty()) continue;

            List<Component> lines = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                null,
                TooltipFlag.NORMAL
            );

            for (Component line : lines) {
                String text = line.getString();
                if (confirm) {
                    if (text.equalsIgnoreCase("Confirm")) return i;
                } else {
                    if (text.contains(needle)) return i;
                }
            }
        }
        return null;
    }

    private static boolean matchesRegex(String regex, String msg) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(msg).find();
        } catch (Exception e) {
            return false;
        }
    }

    private void msgFeedback(String text) {
        if (!feedback.get() || mc.player == null) return;
        mc.player.displayClientMessage(Component.literal(text), false);
    }

    private long parseAmount(String input) {
        if (input == null || input.isEmpty()) return 0;
        String s = input.trim().replace(",", "").replace("$", "").toUpperCase();
        try {
            if (s.endsWith("B")) return (long) (Double.parseDouble(s.replace("B", "")) * 1_000_000_000);
            if (s.endsWith("M")) return (long) (Double.parseDouble(s.replace("M", "")) * 1_000_000);
            if (s.endsWith("K")) return (long) (Double.parseDouble(s.replace("K", "")) * 1_000);
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
