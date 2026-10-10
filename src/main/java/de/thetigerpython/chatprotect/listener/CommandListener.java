package de.thetigerpython.chatprotect.listener;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.checks.impl.*;
import de.thetigerpython.chatprotect.checks.impl.SpamBotCheck;
import de.thetigerpython.chatprotect.checks.impl.SpamCheck;
import de.thetigerpython.chatprotect.checks.impl.BlacklistCheck;
import de.thetigerpython.chatprotect.checks.impl.ToxicityCheck;
import de.thetigerpython.chatprotect.utils.Config;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import de.thetigerpython.chatprotect.utils.services.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {

    public final List<Check> checks = List.of(
            new SpamBotCheck(),
            new SpamCheck(),
            new AdvertisingCheck(),
            new BlacklistCheck(),
            new ToxicityCheck()
    );

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());

        if (event.isCancelled()) return;
        if (player.hasPermission("chatprotect.bypass")) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            message = message.replace(p.getName(), "");
        }

        for (String command : Config.COMMANDS) {
            if (message.startsWith("/" + command + " ")) {
                String content = message.substring(command.length() + 2);

                for (Check check : checks) {
                    CheckResult result = check.checkMessage(player, content);
                    if (result.isFailed()) {
                        Bukkit.getScheduler().runTask(ChatProtect.getInstance(), () -> {
                            PunishmentService.handleViolation(player, content, check, result);
                            LoggingService.log(player, content, check, result);
                        });

                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

    }

}