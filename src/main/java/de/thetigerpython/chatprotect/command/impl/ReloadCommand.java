package de.thetigerpython.chatprotect.command.impl;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.command.ChatProtectCommand;
import de.thetigerpython.chatprotect.listener.ChatListener;
import de.thetigerpython.chatprotect.listener.CommandListener;
import de.thetigerpython.chatprotect.utils.services.BlacklistService;
import de.thetigerpython.chatprotect.utils.Config;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends ChatProtectCommand {

    public ReloadCommand() {
        super("reload", "chatprotect.reload", "Reload the configuration and checks", false);
    }

    @Override
    protected boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ChatProtect.getInstance().saveDefaultConfig();
        ChatProtect.getInstance().reloadConfig();
        BlacklistService.init();
        Config.init(ChatProtect.getInstance());

        ChatProtect.getListeners().forEach(listener -> {
            if (listener instanceof ChatListener chatListener) chatListener.checks.forEach(Check::reloadConfig);
            if (listener instanceof CommandListener commandListener) commandListener.checks.forEach(Check::reloadConfig);
        });

        LoggingService.reload();

        commandSender.sendMessage(Config.PREFIX+"The config.yml and blacklist.yml file have been reloaded!");
        commandSender.sendMessage(Config.PREFIX+"All changes have been applied!");

        return true;
    }

}