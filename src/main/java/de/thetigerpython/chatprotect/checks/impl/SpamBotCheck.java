package de.thetigerpython.chatprotect.checks.impl;

import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpamBotCheck extends Check implements Listener {

    private final Map<UUID, Long> lastMessageMap = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Long>> lastDelaysMap = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Double>> lastWPMMap = new ConcurrentHashMap<>();

    public SpamBotCheck() {
        super("Spam");
    }

    @Override
    public CheckResult checkMessage(Player player, String string) {
        if (!isEnabled()) return new CheckResult();
        if (Config.SPAMMING_MAX_WPM < 1) return new CheckResult();

        UUID uuid = player.getUniqueId();
        CheckResult result = new CheckResult();

        long now = System.currentTimeMillis();

        long lastMessage = lastMessageMap.getOrDefault(uuid, 0L);
        Deque<Long> lastDelays = lastDelaysMap.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        Deque<Double> lastWPM = lastWPMMap.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long delay = now - lastMessage;

        if (lastMessage != 0) {
            String normalized = string.replaceAll("(.)\\1{2,}", "$1");

            if (normalized.length() > 4) {
                double words = normalized.length() / 5.0;
                double minutes = delay / 60000.0;
                double wpm = (minutes > 0) ? words / minutes : 0;

                lastWPM.add(wpm);
                if (lastWPM.size() > 5) {
                    double average = getAverage(lastWPM);
                    if (average > Config.SPAMMING_MAX_WPM && wpm > Config.SPAMMING_MAX_WPM) {
                        result.fail("Inhuman typing speed: " + Math.round(wpm) + " WPM (see maxWPM in the config.yml)", Config.SPAMMING_MESSAGES_SPEED);
                    }
                    lastWPM.removeFirst();
                }
            }
        }

        lastDelays.add(delay);
        if (lastDelays.size() > 5) {
            double std = getStandardDeviation(lastDelays);

            if (std < 10.0) {
                result.fail("Robotic timing in messages detected (std: " + String.format("%.2f", std) + "ms)", Config.SPAMMING_MESSAGES_SPEED);
            }

            lastDelays.removeFirst();
        }

        lastMessageMap.put(uuid, now);
        return result;
    }

    @Override
    public void reloadConfig() {
        setEnabled(Config.SPAMMING_ENABLED);
        setPunishments(Config.SPAMMING_PUNISHMENTS);
    }

    private double getStandardDeviation(Collection<? extends Number> data) {
        if (data.isEmpty()) return 0.0;
        double size = data.size();
        double average = getAverage(data);
        double std = 0.0;
        for (Number n : data) std += Math.pow(n.doubleValue() - average, 2.0);
        return Math.sqrt(std / size);

    }

    public double getAverage(Collection<? extends Number> data) {
        return data.stream().mapToDouble(Number::doubleValue).average().orElse(0D);
    }

}