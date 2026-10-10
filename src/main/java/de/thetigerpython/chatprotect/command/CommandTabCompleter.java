package de.thetigerpython.chatprotect.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();

        if (args.length == 1) {
            for (ChatProtectCommand subcommand : CommandManager.subcommands) {
                if (commandSender.hasPermission(subcommand.getPermission()) || commandSender.hasPermission("chatprotect.*")) {
                    if (subcommand.getName().toLowerCase().startsWith(input)) results.add(subcommand.getName());
                }
            }
        } else if (args.length == 2) {
            for (ChatProtectCommand subcommand : CommandManager.subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName()) && subcommand.isRequiresPlayer()) {
                    Bukkit.getOnlinePlayers().forEach(player -> results.add(player.getName()));
                }
            }
        }

        return results;
    }

}