package de.thetigerpython.chatprotect;

import de.thetigerpython.chatprotect.command.CommandManager;
import de.thetigerpython.chatprotect.command.CommandTabCompleter;
import de.thetigerpython.chatprotect.listener.ChatListener;
import de.thetigerpython.chatprotect.listener.CommandListener;
import de.thetigerpython.chatprotect.listener.JoinListener;
import de.thetigerpython.chatprotect.utils.Metrics;
import de.thetigerpython.chatprotect.utils.services.BlacklistService;
import de.thetigerpython.chatprotect.utils.services.LoggingService;
import de.thetigerpython.chatprotect.utils.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatProtect extends JavaPlugin {

    @Getter
    private static ChatProtect instance;

    @Getter
    public static List<Listener> listeners = new ArrayList<>();

    @Getter
    public static List<UUID> alertsEnabled = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Config.init(this);;
        BlacklistService.init();

        new Metrics(this, 22476);

        initializeListener();
        initializeCommands();
    }

    public static void initializeListener() {
        for (Listener listener : ChatProtect.getListeners()) {
            HandlerList.unregisterAll(listener);
        }
        listeners.clear();

        registerListener(new ChatListener());
        registerListener(new CommandListener());
        registerListener(new JoinListener());
    }

    public static void initializeCommands() {
        PluginCommand chatProtectCommand = Bukkit.getPluginCommand("chatprotect");

        if (chatProtectCommand == null) {
            Bukkit.getLogger().severe("Failed to initialize ChatProtect command!");
            return;
        }

        chatProtectCommand.setExecutor(new CommandManager());
        chatProtectCommand.setTabCompleter(new CommandTabCompleter());
    }

    public static void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, ChatProtect.getInstance());
        listeners.add(listener);
    }

    @Override
    public void onDisable() {
        LoggingService.closeMongoClient();
    }

}
