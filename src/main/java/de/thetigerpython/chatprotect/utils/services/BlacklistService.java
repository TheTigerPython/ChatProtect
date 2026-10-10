package de.thetigerpython.chatprotect.utils.services;

import de.thetigerpython.chatprotect.ChatProtect;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BlacklistService {

    @Getter
    private static List<String> blacklistedWords;

    private static final String BASE_URL = "https://raw.githubusercontent.com/thisandagain/washyourmouthoutwithsoap/develop/data/";

    private static final String[] LIST_FILES = {
            "_en.json",
    };

    public static void init() {
        File file = new File(ChatProtect.getInstance().getDataFolder(), "blacklist.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                Set<String> words = new HashSet<>();
                for (String fname : LIST_FILES) {
                    words.addAll(fetchWordsFromJson(BASE_URL + fname));
                }

                List<String> list = new ArrayList<>();
                for (String w : words) list.add(w.toLowerCase(Locale.ROOT));
                Collections.sort(list);

                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("words", list);
                yaml.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        blacklistedWords = config.getStringList("words");
    }

    private static List<String> fetchWordsFromJson(String urlStr) {
        List<String> list = new ArrayList<>();

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }

            String content = json.toString()
                    .replace("{", "")
                    .replace("}", "")
                    .replace("\"", "");

            for (String entry : content.split(",")) {
                String word = entry.split(":")[0].trim();
                if (!word.isEmpty()) list.add(word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}
