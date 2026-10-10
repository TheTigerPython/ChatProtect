package de.thetigerpython.chatprotect.listener;

import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("chatprotect.alerts") && Config.AUTOMATICALLY_ENABLE_ALERTS) {
            if (!ChatProtect.getAlertsEnabled().contains(player.getUniqueId())) {
                ChatProtect.getAlertsEnabled().add(player.getUniqueId());
            }
        }
    }

}