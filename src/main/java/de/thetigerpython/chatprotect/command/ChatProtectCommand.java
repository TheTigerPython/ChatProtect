package de.thetigerpython.chatprotect.command;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ChatProtectCommand {

    private final String name;
    private final String permission;
    private final String description;

    private final boolean requiresPlayer;

    public ChatProtectCommand(String name, String permission, String description, boolean requiresPlayer) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.requiresPlayer = requiresPlayer;
    }

    protected abstract boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args);

}