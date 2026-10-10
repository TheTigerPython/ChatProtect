package de.thetigerpython.chatprotect.checks.impl;

import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.entity.Player;

import java.util.*;

public class SpamCheck extends Check {

    private final Map<UUID, Long> lastMessage = new HashMap<>();
    private final Map<UUID, Deque<String>> history = new HashMap<>();

    public SpamCheck() {
        super("Spam");
    }

    @Override
    public CheckResult checkMessage(Player player, String message) {
        if (!isEnabled()) return new CheckResult();

        CheckResult result = new CheckResult();
        long now = System.currentTimeMillis();

        String normalized = normalize(message);

        checkCooldown(player, now, result);
        checkUppercase(message, result);
        checkRepetition(player, normalized, result);

        history
                .computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>())
                .addLast(normalized);

        if (history.get(player.getUniqueId()).size() > 6) {
            history.get(player.getUniqueId()).removeFirst();
        }

        lastMessage.put(player.getUniqueId(), now);
        return result;
    }

    @Override
    public void reloadConfig() {
        setEnabled(Config.SPAMMING_ENABLED);
        setPunishments(Config.SPAMMING_PUNISHMENTS);
    }

    private void checkCooldown(Player player, long now, CheckResult result) {
        Long last = lastMessage.get(player);
        if (last == null) return;

        long diff = now - last;
        if (diff < Config.SPAMMING_MIN_COOLDOWN) {
            if (buffer.increase(player) > 3 || diff < Config.SPAMMING_MIN_COOLDOWN / 4) {
                result.fail("Message sent too fast (" + diff + "ms delay) (see minMessageCooldown in config.yml)", Config.SPAMMING_MESSAGES_SPEED);
            }
        } else buffer.decrease(player);
    }

    private void checkUppercase(String message, CheckResult result) {
        int letters = 0, upper = 0;

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) upper++;
            }
        }

        if (letters >= 5 && ((float) upper / letters) > Config.SPAMMING_MAX_UPPERCASE) {
            result.fail("High uppercase ratio (" + (int) ((float)upper/letters * 100) + "%) (see maxUppercasePercentage in config.yml)", Config.SPAMMING_MESSAGES_UPPERCASE);
        }
    }

    private void checkRepetition(Player player, String current, CheckResult result) {
        Deque<String> msgs = history.get(player);
        if (msgs == null) return;
        if (current.length() < 3) return;

        for (String old : msgs) {
            float similarity = similarity(old, current);

            if (similarity >= Config.SPAMMING_MAX_SIMILARITY) {
                if (buffer.increase(player) > 2) {
                    result.fail("Message similar to history (" + (int) (similarity * 100) + "%) (see maxSimilarityPercentage in config.yml)", Config.SPAMMING_MESSAGES_REPEAT);
                    return;
                }
            }
        }
    }

    private float similarity(String a, String b) {
        int common = 0;
        int min = Math.min(a.length(), b.length());

        for (int i = 0; i < min; i++) {
            if (a.charAt(i) == b.charAt(i)) common++;
        }
        return (float) common / Math.max(a.length(), b.length());
    }

    private String normalize(String msg) {
        return cleanUp(msg).getFirst();
    }
}