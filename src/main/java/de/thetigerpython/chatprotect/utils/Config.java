package de.thetigerpython.chatprotect.utils;

import com.tchristofferson.configupdater.ConfigUpdater;
import de.thetigerpython.chatprotect.ChatProtect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class Config {

    // Settings
    public static String PREFIX;
    public static List<String> COMMANDS;
    public static List<String> ALERTS_HOVER_FORMAT;
    public static String ALERTS_COMMAND_SUGGESTION;
    public static String ALERTS_FORMAT;
    public static boolean AUTOMATICALLY_ENABLE_ALERTS;

    // Logs
    public static String LOG_TYPE;
    public static String LOG_FORMAT;
    public static String MONGO_URI;
    public static String MONGO_DATABASE;
    public static String MONGO_COLLECTION;

    // Discord
    public static boolean DISCORD_ENABLED;
    public static String DISCORD_WEBHOOK;
    public static String DISCORD_NAME;
    public static String DISCORD_FOOTER;
    public static boolean DISCORD_INLINE;
    public static List<String> DISCORD_CONTENT;

    // Checks - Toxicity
    public static boolean TOXICITY_ENABLED;
    public static String TOXICITY_MESSAGE;
    public static boolean TOXICITY_DETECT_LEETSPEAK;
    public static boolean USE_CLOUD_DETECTION;
    public static double CLOUD_DETECTION_THRESHOLD;
    public static List<String> TOXICITY_PUNISHMENTS;

    // Checks - Spamming
    public static boolean SPAMMING_ENABLED;
    public static String SPAMMING_MESSAGES_SPEED;
    public static String SPAMMING_MESSAGES_UPPERCASE;
    public static String SPAMMING_MESSAGES_REPEAT;
    public static int SPAMMING_MIN_COOLDOWN;
    public static double SPAMMING_MAX_UPPERCASE;
    public static double SPAMMING_MAX_SIMILARITY;
    public static int SPAMMING_MAX_WPM;
    public static List<String> SPAMMING_PUNISHMENTS;

    // Checks - Advertising
    public static boolean ADVERTISING_ENABLED;
    public static String ADVERTISING_MESSAGE;
    public static boolean ADVERTISING_ADVANCED;
    public static double ADVERTISING_THRESHOLD;
    public static List<String> ADVERTISING_WHITELIST;
    public static List<String> ADVERTISING_PUNISHMENTS;

    public static void init(ChatProtect plugin) {
        plugin.saveDefaultConfig();

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        try {
            ConfigUpdater.update(plugin, "config.yml", configFile, Collections.emptyList());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to auto-update config.yml: " + e.getMessage());
        }

        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        // Settings
        PREFIX = translate(cfg.getString("settings.prefix", "&9ChatProtect &8» &7"));
        COMMANDS = cfg.getStringList("settings.commands");
        ALERTS_HOVER_FORMAT = cfg.getStringList("settings.alertsHoverFormat");
        ALERTS_FORMAT = translate(cfg.getString("settings.alertsFormat", "&9%player% &7| &9\"%message%\""));
        ALERTS_COMMAND_SUGGESTION = cfg.getString("settings.alertsCommandSuggestion", "tempmute %player% 10m %check%");
        AUTOMATICALLY_ENABLE_ALERTS = cfg.getBoolean("settings.automaticallyEnabledAlerts", true);

        // Logs
        LOG_TYPE = cfg.getString("logs.type", "FILES").toUpperCase();
        LOG_FORMAT = cfg.getString("logs.format", "yyyy-MM-dd HH:mm:ss");
        MONGO_URI = cfg.getString("logs.mongoDB", "");
        MONGO_DATABASE = cfg.getString("logs.database", "chatprotect");
        MONGO_COLLECTION = cfg.getString("logs.collection", "logs");

        // Discord
        DISCORD_ENABLED = cfg.getBoolean("discord.enabled", false);
        DISCORD_WEBHOOK = cfg.getString("discord.webhook", "");
        DISCORD_NAME = cfg.getString("discord.name", "⚠️ » Chat Protect Alert « ⚠️");
        DISCORD_FOOTER = cfg.getString("discord.footer", "ChatProtect - Automatic chat moderation");
        DISCORD_INLINE = cfg.getBoolean("discord.inline", false);
        DISCORD_CONTENT = cfg.getStringList("discord.content");

        // Checks - Toxicity
        TOXICITY_ENABLED = cfg.getBoolean("checks.toxicity.enabled", true);
        TOXICITY_MESSAGE = translate(cfg.getString("checks.toxicity.message", "Please watch your language!"));
        TOXICITY_DETECT_LEETSPEAK = cfg.getBoolean("checks.toxicity.detectLeetspeak", true);
        USE_CLOUD_DETECTION = cfg.getBoolean("checks.toxicity.useCloudDetection", false);
        CLOUD_DETECTION_THRESHOLD = cfg.getDouble("checks.toxicity.cloudDetectionThreshold", 0.6);
        TOXICITY_PUNISHMENTS = cfg.getStringList("checks.toxicity.punishments");

        // Checks - Spamming
        SPAMMING_ENABLED = cfg.getBoolean("checks.spamming.enabled", true);
        SPAMMING_MESSAGES_SPEED = translate(cfg.getString("checks.spamming.messages.speed", "Please don't spam the chat!"));
        SPAMMING_MESSAGES_UPPERCASE = translate(cfg.getString("checks.spamming.messages.uppercase", "Please don't use excessive uppercase letters!"));
        SPAMMING_MESSAGES_REPEAT = translate(cfg.getString("checks.spamming.messages.repeat", "You have already said that before!"));
        SPAMMING_MIN_COOLDOWN = cfg.getInt("checks.spamming.minMessageCooldown", 500);
        SPAMMING_MAX_UPPERCASE = cfg.getDouble("checks.spamming.maxUppercasePercentage", 0.4);
        SPAMMING_MAX_SIMILARITY = cfg.getDouble("checks.spamming.maxSimilarityPercentage", 0.7);
        SPAMMING_MAX_WPM = cfg.getInt("checks.spamming.maxWPM", 200);
        SPAMMING_PUNISHMENTS = cfg.getStringList("checks.spamming.punishments");

        // Checks - Advertising
        ADVERTISING_ENABLED = cfg.getBoolean("checks.advertising.enabled", true);
        ADVERTISING_MESSAGE = translate(cfg.getString("checks.advertising.message", "Advertising is not allowed on our network!"));
        ADVERTISING_ADVANCED = cfg.getBoolean("checks.advertising.advancedChecks", false);
        ADVERTISING_THRESHOLD = cfg.getDouble("checks.advertising.threshold", 0.75);
        ADVERTISING_WHITELIST = cfg.getStringList("checks.advertising.whitelist");
        ADVERTISING_PUNISHMENTS = cfg.getStringList("checks.advertising.punishments");
    }

    private static String translate(String input) {
        if (input == null) return "";
        String translated = ColorFormatter.translateHexColorCodes(input);
        return ChatColor.translateAlternateColorCodes('&', translated);
    }
}