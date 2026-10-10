package de.thetigerpython.chatprotect.command;

import de.thetigerpython.chatprotect.command.impl.*;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager implements CommandExecutor {

    public static ChatProtectCommand[] subcommands = {
            new AlertsCommand(),
            new LogsCommand(),
            new ClearLogsCommand(),
            new ClearChatCommand(),
            new ReloadCommand(),
    };

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        for (ChatProtectCommand subcommand : subcommands)  {
            if (args.length >= 1 && subcommand.getName().toLowerCase().equalsIgnoreCase(args[0]) && (commandSender.hasPermission(subcommand.getPermission()) || commandSender.hasPermission("chatprotect.*"))) {
                subcommand.handle(commandSender, command, s, args);
                return true;
            }
        }

        sendDefaultMessage(commandSender);

        return false;
    }

    private void sendDefaultMessage(CommandSender sender) {
        List<ChatProtectCommand> availableSubCommands = new ArrayList<>();
        Arrays.stream(subcommands).forEach(subcommand -> {
            if (sender.hasPermission(subcommand.getPermission())) availableSubCommands.add(subcommand);
        });

        if (availableSubCommands.isEmpty()) {
            sender.sendMessage(Config.PREFIX+"You don't have permission to execute that command!");
        } else {
            sender.sendMessage(Config.PREFIX+"Available commands: "+ChatColor.GRAY+"("+availableSubCommands.size()+")");

            availableSubCommands.forEach(subcommand -> sender.sendMessage(Config.PREFIX+"- "+subcommand.getName()+ChatColor.GRAY+" ("+subcommand.getDescription()+") ("+subcommand.getPermission()+")"));
        }
    }

}