package de.thetigerpython.chatprotect.utils.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudApiService {

    private final String[] CATEGORIES = {
            "harassment", "harassment/threatening", "hate", "hate/threatening",
            "illicit", "illicit/violent", "self-harm", "self-harm/instructions",
            "self-harm/intent", "sexual", "sexual/minors", "violence", "violence/graphic"
    };

    public double getToxicityScore(String message) {
        try {
            URL url = new URL("https://chatprotect.liebschju.workers.dev/v2");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonPayload = "{\"input\":" + escapeJsonString(message) + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            InputStream stream = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return parseMaxCategoryScore(response.toString());

        } catch (Exception e) {
            return 0.0;
        }
    }

    private double parseMaxCategoryScore(String json) {
        double maxScore = 0.0;
        int scoresIdx = json.indexOf("\"category_scores\"");
        if (scoresIdx < 0) return maxScore;

        for (String key : CATEGORIES) {
            int keyIdx = json.indexOf("\"" + key + "\":", scoresIdx);
            if (keyIdx >= 0) {
                int start = keyIdx + key.length() + 3;
                int end = json.indexOf(",", start);
                if (end < 0) end = json.indexOf("}", start);

                if (end > start) {
                    try {
                        double val = Double.parseDouble(json.substring(start, end).trim());
                        if (val > maxScore) {
                            maxScore = val;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return maxScore;
    }

    private String escapeJsonString(String input) {
        if (input == null) return "\"\"";
        return "\"" + input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

}
