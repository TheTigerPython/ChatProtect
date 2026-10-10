package de.thetigerpython.chatprotect.listener;

import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.checks.impl.*;
import de.thetigerpython.chatprotect.checks.impl.SpamBotCheck;
import de.thetigerpython.chatprotect.checks.impl.SpamCheck;
import de.thetigerpython.chatprotect.checks.impl.BlacklistCheck;
import de.thetigerpython.chatprotect.checks.impl.ToxicityCheck;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import de.thetigerpython.chatprotect.utils.services.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {

    public final List<Check> checks = List.of(
            new SpamBotCheck(),
            new SpamCheck(),
            new AdvertisingCheck(),
            new BlacklistCheck(),
            new ToxicityCheck()
    );

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());

        if (event.isCancelled()) return;
        if (player.hasPermission("chatprotect.bypass")) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            message = message.replace(p.getName(), "");
        }

        for (Check check : checks) {
            CheckResult result = check.checkMessage(player, message);
            if (result.isFailed()) {
                PunishmentService.handleViolation(player, message, check, result);
                LoggingService.log(player, message, check, result);

                event.setCancelled(true);
                return;
            }
        }
    }

}