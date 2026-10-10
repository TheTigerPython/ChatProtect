package de.thetigerpython.chatprotect.command.impl;

import de.thetigerpython.chatprotect.command.ChatProtectCommand;
import de.thetigerpython.chatprotect.utils.Config;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ClearLogsCommand extends ChatProtectCommand {

    public ClearLogsCommand() {
        super("clearlogs", "chatprotect.clearlogs", "Clear logs for a specific player", true);
    }

    @Override
    protected boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length <= 1) {
            commandSender.sendMessage(Config.PREFIX + "Please use: /chatprotect clearlogs <player>");
            return false;
        }

        if (Config.LOG_TYPE.equalsIgnoreCase("NONE")) {
            commandSender.sendMessage(Config.PREFIX + "Logs are disabled in the configuration.");
            return false;
        }

        String target = args[1];
        commandSender.sendMessage(Config.PREFIX + "Clearing logs for " + target + "...");

        LoggingService.clearLogs(target, (Long deletedCount) -> {
            if (deletedCount == 0) {
                commandSender.sendMessage(Config.PREFIX + "No logs found to clear for " + target + ".");
            } else {
                commandSender.sendMessage(Config.PREFIX + "Successfully deleted " + deletedCount + " log entries for " + target + ".");
            }
        });

        return true;
    }
}