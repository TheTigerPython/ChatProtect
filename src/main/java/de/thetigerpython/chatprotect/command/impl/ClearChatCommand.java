package de.thetigerpython.chatprotect.command.impl;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.command.ChatProtectCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ClearChatCommand extends ChatProtectCommand {

    private final Random random = new Random();

    public ClearChatCommand() {
        super("clearchat", "chatprotect.clearchat", "Clear the chat", false);
    }

    @Override
    protected boolean handle(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            for (int i = 0; i < 100; i++) {
                StringBuilder message = new StringBuilder(" ");
                int amount = random.nextInt(500);
                message.append(" ".repeat(amount));
                
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(message.toString());
                });
            }
        });

        return true;
    }
}