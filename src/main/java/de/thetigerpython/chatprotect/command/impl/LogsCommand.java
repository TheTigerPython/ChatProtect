package de.thetigerpython.chatprotect.command.impl;

import de.thetigerpython.chatprotect.command.ChatProtectCommand;
import de.thetigerpython.chatprotect.utils.Config;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class LogsCommand extends ChatProtectCommand {

    private static final int PAGE_SIZE = 10;

    public LogsCommand() {
        super("logs", "chatprotect.logs", "View a player's chat logs", true);
    }

    @Override
    protected boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Config.PREFIX + "This subcommand can only be used by players.");
            return false;
        }

        if (args.length <= 1) {
            player.sendMessage(Config.PREFIX + "Please use: /chatprotect logs <player> [page]");
            return false;
        }

        if (Config.LOG_TYPE.equalsIgnoreCase("NONE")) {
            player.sendMessage(Config.PREFIX + "Logs are not setup. Please check the config.yml file!");
            return false;
        }

        String target = args[1];
        int pageInput = 1;

        if (args.length >= 3) {
            try {
                pageInput = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {}
        }

        final int page = Math.max(1, pageInput);

        player.sendMessage(Config.PREFIX + "Searching logs for " + target + "...");

        LoggingService.getLogs(target, (List<String> results) -> {
            if (results.isEmpty()) {
                player.sendMessage(Config.PREFIX + "No logs found for " + target + "!");
                return;
            }

            int totalLogs = results.size();
            int totalPages = (int) Math.ceil((double) totalLogs / PAGE_SIZE);
            int currentPage = Math.min(page, totalPages);

            int startIndex = (currentPage - 1) * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, totalLogs);
            List<String> pageResults = results.subList(startIndex, endIndex);

            player.sendMessage("");

            if (totalPages > 1) {
                player.sendMessage(Config.PREFIX + "Found " + totalLogs + " log entries (Page " + currentPage + "/" + totalPages + "):");
            } else {
                player.sendMessage(Config.PREFIX + "Found " + totalLogs + " log entries:");
            }

            int failed = 0;
            for (String line : pageResults) {
                try {
                    String timestamp = line.substring(1, line.indexOf("]"));
                    String username = line.split("\\[Player: ")[1].split(" \\(")[0];
                    String checkName = line.split("\\[Check: ")[1].split("] \\[Reason:")[0];
                    String reason = line.split("\\[Reason: ")[1].split("] Message:")[0];
                    String message = line.split(" Message: ")[1];

                    String alertText = ChatColor.translateAlternateColorCodes('&', Config.ALERTS_FORMAT
                                    .replace("%player%", username)
                                    .replace("%message%", message)
                                    .replace("%check%", checkName))
                            .replace("%reason%", reason);

                    String formattedHover = Config.ALERTS_HOVER_FORMAT.stream()
                            .map(hoverLine -> hoverLine
                                    .replace("%player%", username)
                                    .replace("%message%", message)
                                    .replace("%check%", checkName)
                                    .replace("%reason%", reason))
                            .map(hoverLine -> ChatColor.translateAlternateColorCodes('&', hoverLine))
                            .collect(Collectors.joining("\n"));

                    String formattedSuggestion = Config.ALERTS_COMMAND_SUGGESTION
                            .replace("%player%", username)
                            .replace("%message%", message)
                            .replace("%check%", checkName)
                            .replace("%reason%", reason);

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

                    TextComponent completeComponent = new TextComponent(ChatColor.GRAY + "» [" + timestamp + "] ");
                    completeComponent.addExtra(messageComponent);

                    player.spigot().sendMessage(completeComponent);
                } catch (Exception e) {
                    failed++;
                }
            }

            if (failed > 0) {
                player.sendMessage(Config.PREFIX + "Failed to load " + failed + " logs on this page!");
            }

            if (totalPages > 1) {
                String alertColorHex = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&', Config.ALERTS_FORMAT));
                if (alertColorHex.isEmpty()) {
                    alertColorHex = ChatColor.RED.toString();
                }

                TextComponent footer = new TextComponent("");

                if (currentPage > 1) {
                    TextComponent prev = new TextComponent(TextComponent.fromLegacyText(alertColorHex + "« Previous"));
                    prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cp logs " + target + " " + (currentPage - 1)));
                    prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(alertColorHex + "Click to go to page " + (currentPage - 1))));
                    footer.addExtra(prev);
                } else {
                    TextComponent prevDisabled = new TextComponent(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "« Previous"));
                    footer.addExtra(prevDisabled);
                }

                TextComponent pageIndicator = new TextComponent(TextComponent.fromLegacyText(ChatColor.GRAY + " | Page " + currentPage + "/" + totalPages + " | "));
                footer.addExtra(pageIndicator);

                if (currentPage < totalPages) {
                    TextComponent next = new TextComponent(TextComponent.fromLegacyText(alertColorHex + "Next »"));
                    next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cp logs " + target + " " + (currentPage + 1)));
                    next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(alertColorHex + "Click to go to page " + (currentPage + 1))));
                    footer.addExtra(next);
                } else {
                    TextComponent nextDisabled = new TextComponent(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "Next »"));
                    footer.addExtra(nextDisabled);
                }

                player.sendMessage("");
                player.spigot().sendMessage(footer);
            }

            player.sendMessage("");
        });

        return true;
    }
}