package de.thetigerpython.chatprotect.utils.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import de.thetigerpython.chatprotect.ChatProtect;
import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.Config;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoggingService {
    
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;
    private static String currentMongoUri = "";

    private static boolean validateMongoClient() {
        if (Config.MONGO_URI == null || Config.MONGO_URI.isBlank()) {
            closeMongoClient();
            return false;
        }

        if (mongoClient != null && !Config.MONGO_URI.equals(currentMongoUri)) {
            closeMongoClient();
        }

        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(Config.MONGO_URI);
                database = mongoClient.getDatabase(Config.MONGO_DATABASE);
                currentMongoUri = Config.MONGO_URI;

                ensureIndexes();
            } catch (Exception e) {
                ChatProtect.getInstance().getLogger().severe("Failed to establish MongoDB connection: " + e.getMessage());
                closeMongoClient();
                return false;
            }
        }

        try {
            database.runCommand(new Document("ping", 1));
        } catch (Exception e) {
            ChatProtect.getInstance().getLogger().severe("MongoDB connection validation ping failed: " + e.getMessage());
            closeMongoClient();
            return false;
        }

        return true;
    }

    private static void ensureIndexes() {
        if (database == null) return;
        MongoCollection<Document> collection = database.getCollection(Config.MONGO_COLLECTION);
        collection.createIndex(Indexes.ascending("username"));
        collection.createIndex(Indexes.ascending("uuid"));
    }

    public static void closeMongoClient() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception ignored) {}
            mongoClient = null;
            database = null;
            currentMongoUri = "";
        }
    }

    public static void log(Player player, String message, Check check, CheckResult result) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            if (Config.LOG_TYPE.equalsIgnoreCase("MONGODB")) {
                logToMongoDB(player, message, check, result);
            } else if (Config.LOG_TYPE.equalsIgnoreCase("FILES")) {
                logToFile(player, message, check, result);
            } else if (!Config.LOG_TYPE.equalsIgnoreCase("NONE")) {
                ChatProtect.getInstance().getLogger().severe("Invalid LOG_TYPE configured: " + Config.LOG_TYPE);
            }
        });
    }

    public static void getLogs(String targetIdentifier, Consumer<List<String>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            List<String> matchedLogs;

            if (Config.LOG_TYPE.equalsIgnoreCase("MONGODB")) {
                matchedLogs = fetchLogsFromMongoDB(targetIdentifier);
            } else if (Config.LOG_TYPE.equalsIgnoreCase("FILES")) {
                matchedLogs = fetchLogsFromFile(targetIdentifier);
            } else {
                matchedLogs = new ArrayList<>();
            }

            Bukkit.getScheduler().runTask(ChatProtect.getInstance(), () -> callback.accept(matchedLogs));
        });
    }

    public static void clearLogs(String targetIdentifier, Consumer<Long> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), () -> {
            long deletedCount = 0;

            if (Config.LOG_TYPE.equalsIgnoreCase("MONGODB")) {
                deletedCount = clearLogsFromMongoDB(targetIdentifier);
            } else if (Config.LOG_TYPE.equalsIgnoreCase("FILES")) {
                deletedCount = clearLogsFromFile(targetIdentifier);
            }

            long finalCount = deletedCount;
            Bukkit.getScheduler().runTask(ChatProtect.getInstance(), () -> callback.accept(finalCount));
        });
    }

    private static void logToFile(Player player, String message, Check check, CheckResult result) {
        ChatProtect plugin = ChatProtect.getInstance();
        File logFile = new File(plugin.getDataFolder(), "logs.txt");

        try {
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                String timestamp = LocalDateTime.now().format(getFormatter());
                writer.printf("[%s] [Player: %s (%s)] [Check: %s] [Reason: %s] Message: %s%n",
                        timestamp,
                        player.getName(),
                        player.getUniqueId(),
                        check.getSimpleName(),
                        result.getInfo(),
                        message
                );
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to logs.txt: " + e.getMessage());
        }
    }

    private static List<String> fetchLogsFromFile(String target) {
        List<String> matches = new ArrayList<>();
        File logFile = new File(ChatProtect.getInstance().getDataFolder(), "logs.txt");

        if (!logFile.exists()) return matches;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            String lowerTarget = target.toLowerCase();
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("player: " + lowerTarget) || line.contains(target)) {
                    matches.add(line);
                    if (matches.size() >= 1000) break;
                }
            }
        } catch (IOException e) {
            ChatProtect.getInstance().getLogger().severe("Failed to read logs.txt: " + e.getMessage());
        }

        return matches;
    }

    private static long clearLogsFromFile(String target) {
        File logFile = new File(ChatProtect.getInstance().getDataFolder(), "logs.txt");
        if (!logFile.exists()) return 0;

        File tempFile = new File(ChatProtect.getInstance().getDataFolder(), "logs.txt.tmp");
        long deletedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            String lowerTarget = target.toLowerCase();
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("player: " + lowerTarget) || line.contains(target)) {
                    deletedCount++;
                } else {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            ChatProtect.getInstance().getLogger().severe("Failed to clear file logs: " + e.getMessage());
            return 0;
        }

        if (!logFile.delete() || !tempFile.renameTo(logFile)) {
            ChatProtect.getInstance().getLogger().severe("Failed to replace log file after deletion.");
        }

        return deletedCount;
    }

    private static void logToMongoDB(Player player, String message, Check check, CheckResult result) {
        if (!validateMongoClient()) return;

        try {
            MongoCollection<Document> collection = database.getCollection(Config.MONGO_COLLECTION);
            String timestamp = LocalDateTime.now().format(getFormatter());

            Document logDoc = new Document("timestamp", timestamp)
                    .append("username", player.getName())
                    .append("uuid", player.getUniqueId().toString())
                    .append("check", check.getSimpleName())
                    .append("reason", result.getInfo())
                    .append("message", message);

            collection.insertOne(logDoc);
        } catch (Exception e) {
            ChatProtect.getInstance().getLogger().severe("MongoDB document insertion error: " + e.getMessage());
        }
    }

    private static List<String> fetchLogsFromMongoDB(String target) {
        List<String> matches = new ArrayList<>();
        if (!validateMongoClient()) return matches;

        try {
            MongoCollection<Document> collection = database.getCollection(Config.MONGO_COLLECTION);
            Bson queryFilter = Filters.or(
                    Filters.eq("username", target),
                    Filters.eq("uuid", target)
            );

            for (Document doc : collection.find(queryFilter).limit(1000)) {
                String entryLine = String.format("[%s] [Player: %s (%s)] [Check: %s] [Reason: %s] Message: %s",
                        doc.getString("timestamp"),
                        doc.getString("username"),
                        doc.getString("uuid"),
                        doc.getString("check"),
                        doc.getString("reason"),
                        doc.getString("message")
                );
                matches.add(entryLine);
            }
        } catch (Exception e) {
            ChatProtect.getInstance().getLogger().severe("MongoDB logs query operation error: " + e.getMessage());
        }
        return matches;
    }

    private static long clearLogsFromMongoDB(String target) {
        if (!validateMongoClient()) return 0;

        try {
            MongoCollection<Document> collection = database.getCollection(Config.MONGO_COLLECTION);
            Bson queryFilter = Filters.or(
                    Filters.eq("username", target),
                    Filters.eq("uuid", target)
            );

            DeleteResult result = collection.deleteMany(queryFilter);
            return result.getDeletedCount();
        } catch (Exception e) {
            ChatProtect.getInstance().getLogger().severe("MongoDB logs deletion error: " + e.getMessage());
            return 0;
        }
    }

    public static void reload() {
        closeMongoClient();
        if (Config.LOG_TYPE.equalsIgnoreCase("MONGODB")) {
            Bukkit.getScheduler().runTaskAsynchronously(ChatProtect.getInstance(), LoggingService::validateMongoClient);
        }
    }

    private static DateTimeFormatter getFormatter() {
        try {
            return DateTimeFormatter.ofPattern(Config.LOG_FORMAT);
        } catch (IllegalArgumentException | NullPointerException e) {
            ChatProtect.getInstance().getLogger().warning("Invalid logs format pattern provided in config.yml! Falling back to default.");
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
    }
}