package de.thetigerpython.chatprotect.utils.services;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.Config;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PunishmentService {

    public static void handleViolation(Player player, String message, Check check, CheckResult result) {
        if (player == null || check == null) return;

        if (!result.getMessage().isEmpty()) {
            player.sendMessage(Config.PREFIX + result.getMessage());
        }

        HashMap<UUID, Integer> violations = check.getViolations();
        UUID uuid = player.getUniqueId();
        int newCount = violations.getOrDefault(uuid, 0) + 1;
        violations.put(uuid, newCount);
        check.setViolations(violations);

        applyPunishments(player, message, check, result, newCount);
    }

    private static void applyPunishments(Player player, String message, Check check, CheckResult result, int violations) {
        List<String> punishments = check.getPunishments();
        if (punishments == null || punishments.isEmpty()) return;

        for (String line : punishments) {
            try {
                String[] parts = line.split(" ", 2);
                String[] values = parts[0].split(":");
                int threshold = Integer.parseInt(values[0]);
                int interval = Integer.parseInt(values[1]);
                String command = (parts.length > 1 ? parts[1] : "").replace("%player%", player.getName());

                if (violations >= threshold && (violations - threshold) % interval == 0) {
                    executePunishment(player, message, check, result, command);
                }
            } catch (Exception e) {
                ChatProtect.getInstance().getLogger().info("An error occurred: "+e.getMessage());
            }
        }
    }

    private static void executePunishment(Player player, String message, Check check, CheckResult result, String command) {
        switch (command.toLowerCase()) {
            case "[discord]" -> sendDiscordWebhook(player, message, check, result);
            case "[alert]" -> notifyStaff(player, message, check, result);
            default -> Bukkit.getScheduler().runTask(ChatProtect.getInstance(), () -> {
                String cmd = ChatColor.translateAlternateColorCodes('&', command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            });
        }
    }

    private static void sendDiscordWebhook(Player player, String message, Check check, CheckResult result) {
        if (!Config.DISCORD_ENABLED || Config.DISCORD_WEBHOOK == null || Config.DISCORD_WEBHOOK.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            try {
                URL url = new URL(Config.DISCORD_WEBHOOK);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                String title = replacePlaceholders(Config.DISCORD_NAME, player, message, check, result);
                String footer = replacePlaceholders(Config.DISCORD_FOOTER, player, message, check, result);
                List<String> content = Config.DISCORD_CONTENT;

                StringBuilder fieldsBuilder = new StringBuilder();
                StringBuilder descriptionBuilder = new StringBuilder();

                for (String line : content) {
                    if (line == null || line.isBlank()) continue;

                    line = replacePlaceholders(line, player, message, check, result);

                    if (line.contains("|")) {
                        String[] parts = line.split("\\|", 2);
                        String name = parts[0].trim();
                        String value = parts[1].trim();
                        fieldsBuilder.append(String.format("""
                                {
                                  "name": "%s",
                                  "value": "%s",
                                  "inline": %s
                                },
                                """, escapeJson(name), escapeJson(value), Config.DISCORD_INLINE));
                    } else {
                        descriptionBuilder.append(line);
                    }
                }

                String fields = fieldsBuilder.toString().trim();
                if (fields.endsWith(",")) fields = fields.substring(0, fields.length() - 1);

                String description = escapeJson(descriptionBuilder.toString().trim());

                String json = """
                        {
                          "embeds": [{
                            "title": "%s",
                            "description": "%s",
                            "color": 16711680,
                            "fields": [%s],
                            "footer": { "text": "%s" }
                          }]
                        }
                        """.formatted(escapeJson(title), description, fields, escapeJson(footer));

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                ChatProtect.getInstance().getLogger().info("An error occurred: "+e.getMessage());
            }
        });
    }

    private static String replacePlaceholders(String input, Player player, String message, Check check, CheckResult result) {
        if (input == null) return "";
        return input
                .replace("%player%", player.getName())
                .replace("%message%", message)
                .replace("%check%", check.getClass().getSimpleName().replace("Check", ""))
                .replace("%debug%", result.getInfo());
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static void notifyStaff(Player violator, String message, Check check, CheckResult result) {
        String checkName = check.getSimpleName();

        String alertText = ChatColor.translateAlternateColorCodes('&', Config.PREFIX + Config.ALERTS_FORMAT
                .replace("%player%", violator.getName())
                .replace("%message%", message)
                .replace("%check%", checkName))
                .replace("%reason%", result.getInfo());

        List<String> hoverLines = Config.ALERTS_HOVER_FORMAT;

        String formattedHover = hoverLines.stream()
                .map(line -> line
                        .replace("%player%", violator.getName())
                        .replace("%message%", message)
                        .replace("%check%", checkName)
                        .replace("%reason%", result.getInfo()))
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.joining("\n"));

        String formattedSuggestion = Config.ALERTS_COMMAND_SUGGESTION
                .replace("%player%", violator.getName())
                .replace("%message%", message)
                .replace("%check%", checkName)
                .replace("%reason%", result.getInfo());

        TextComponent messageComponent = new TextComponent(TextComponent.fromLegacyText(alertText));
        messageComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText(formattedHover)
        ));

        if (!formattedSuggestion.isEmpty()) {
            messageComponent.setClickEvent(new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/" + formattedSuggestion
            ));
        }

        for (UUID uuid : ChatProtect.getAlertsEnabled()) {
            Player staff = Bukkit.getPlayer(uuid);
            if (staff != null && staff.isOnline() && staff.hasPermission("chatprotect.alerts")) {
                staff.spigot().sendMessage(messageComponent);
            }
        }
    }
}
