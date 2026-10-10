package de.thetigerpython.chatprotect.command.impl;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.command.ChatProtectCommand;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AlertsCommand extends ChatProtectCommand {

    public AlertsCommand() {
        super("alerts", "chatprotect.alerts", "Toggle alerts", false);
    }

    @Override
    protected boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (ChatProtect.getAlertsEnabled().contains(player.getUniqueId())) {
                player.sendMessage(Config.PREFIX+"Alerts have been disabled.");
                ChatProtect.getAlertsEnabled().remove(player.getUniqueId());
            }else {
                player.sendMessage(Config.PREFIX+"Alerts have been enabled.");
                ChatProtect.getAlertsEnabled().add(player.getUniqueId());
            }
        }else {
            commandSender.sendMessage(Config.PREFIX+"This subcommand can only be used by players.");
        }

        return false;
    }

}