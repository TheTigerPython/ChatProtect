package de.thetigerpython.chatprotect.checks.impl;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.services.CloudApiService;
import de.thetigerpython.chatprotect.utils.Config;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import de.thetigerpython.chatprotect.utils.services.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

public class ToxicityCheck extends Check {

    private final CloudApiService cloud = new CloudApiService();

    public ToxicityCheck() {
        super("Toxicity");
    }

    @Override
    public CheckResult checkMessage(Player player, String message) {
        if (!isEnabled() || !Config.TOXICITY_ENABLED || !Config.USE_CLOUD_DETECTION) return new CheckResult();

        long now = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            double probability = cloud.getToxicityScore(message);

            if (probability > Config.CLOUD_DETECTION_THRESHOLD) {
                Bukkit.getScheduler().runTask(ChatProtect.getInstance(), () -> {
                    String formattedPercent = String.format(Locale.US, "%.1f%%", probability * 100);
                    String formattedMaxPercent = String.format(Locale.US, "%.1f%%", Config.CLOUD_DETECTION_THRESHOLD * 100);

                    CheckResult result = new CheckResult().fail(
                            "ChatProtect cloud detected toxic behaviour (" + formattedPercent + " / " + formattedMaxPercent + ", latency: " + (System.currentTimeMillis() - now) + "ms) (see cloudDetectionThreshold in config.yml)",
                            message
                    );

                    PunishmentService.handleViolation(player, message, this, result);
                    LoggingService.log(player, message, this, result);
                });
            }
        });

        return new CheckResult();
    }

    @Override
    public void reloadConfig() {
        setPunishments(Config.TOXICITY_PUNISHMENTS);
        setEnabled(true);
    }
}