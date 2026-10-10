package de.thetigerpython.chatprotect.checks.impl;

import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class AdvertisingCheck extends Check {

    private static final String[] DOMAINS = {
            "com", "org", "io", "net", "mc", "ru", "uk", "pl", "de", "pro", "me", "gg", "tk", "xyz", "biz", "info", "eu", "co"
    };

    private static final String[] HOSTERS = {
            "aternos", "ploudos", "scalacube", "serverpro", "minefort", "minehut",
            "falixnodes", "shockbyte", "bisecthosting", "pebblehost", "wisehosting", "magmanode"
    };

    private static final String[] HACKED_CLIENTS = {
            "liquidbounce", "augustus", "wurst", "futureclient", "impactclient",
            "rusherhack", "vape", "aristois", "horion"
    };

    private static final String[] WEBSITES = {
            "pornhub", "xhamster", "brazzers", "redtube", "onlyfans"
    };

    private static final Pattern URL_PROTOCOL_PATTERN = Pattern.compile("(?i)(https?://|www\\.)");
    private static final String IP_REGEX = "\\b\\d{1,3}(\\s*(\\.|dot|punkt|,|_|-)\\s*)\\d{1,3}\\1\\d{1,3}\\1\\d{1,3}\\b";

    private boolean strictMode;

    public AdvertisingCheck() {
        super("Advertising");
    }

    @Override
    public CheckResult checkMessage(Player player, String message) {
        if (!isEnabled()) return new CheckResult();

        String clean = simpleCleanUp(message);
        clean = stripWhitelistedDomains(clean);

        CheckResult result = new CheckResult();

        if (URL_PROTOCOL_PATTERN.matcher(clean).find()) return result.fail("URL protocol detected", Config.ADVERTISING_MESSAGE);
        if (Pattern.compile(IP_REGEX).matcher(clean).find()) return result.fail("Numerical IP detected", Config.ADVERTISING_MESSAGE);

        if (containsBlacklistedRegex(clean, HOSTERS)) return result.fail("Blacklisted hoster mentioned", Config.ADVERTISING_MESSAGE);
        if (containsBlacklistedRegex(clean, HACKED_CLIENTS)) return result.fail("Hack client mentioned", Config.ADVERTISING_MESSAGE);
        if (containsBlacklistedRegex(clean, WEBSITES)) return result.fail("Harmful link detected", Config.ADVERTISING_MESSAGE);
        if (containsDomainRegex(clean)) return result.fail("External domain detected", Config.ADVERTISING_MESSAGE);

        if (strictMode) {
            String skeleton = clean.replaceAll("[^a-z0-9]", "");

            if (containsBlacklistedSkeleton(skeleton, HOSTERS)) return result.fail("Blacklisted hoster mentioned (see advancedChecks in config.yml)", Config.ADVERTISING_MESSAGE);
            if (containsBlacklistedSkeleton(skeleton, HACKED_CLIENTS)) return result.fail("Hack client mentioned (see advancedChecks in config.yml)", Config.ADVERTISING_MESSAGE);
            if (containsBlacklistedSkeleton(skeleton, WEBSITES)) return result.fail("Harmful link detected (see advancedChecks in config.yml)", Config.ADVERTISING_MESSAGE);
            if (containsDomainSkeleton(skeleton)) return result.fail("External domain detected (see advancedChecks in config.yml)", Config.ADVERTISING_MESSAGE);

            if (skeleton.matches(".*\\d{4,12}.*") && Pattern.compile(IP_REGEX.replace("(\\s*(\\.|dot|punkt|,|_|-)\\s*)", "")).matcher(skeleton).find()) {
                return result.fail("Numerical IP detected (see advancedChecks in config.yml)", Config.ADVERTISING_MESSAGE);
            }
        }

        return result;
    }

    @Override
    public void reloadConfig() {
        setEnabled(Config.ADVERTISING_ENABLED);
        setPunishments(Config.ADVERTISING_PUNISHMENTS);
        this.strictMode = Config.ADVERTISING_ADVANCED;
    }

    private String stripWhitelistedDomains(String text) {
        if (Config.ADVERTISING_WHITELIST == null || Config.ADVERTISING_WHITELIST.isEmpty()) {
            return text;
        }

        String result = text;
        String dotRegex = "\\s*(\\.|dot|punkt|,|_|-)\\s*";

        for (String allowed : Config.ADVERTISING_WHITELIST) {
            if (allowed == null || allowed.isBlank()) continue;

            String normalizedAllowed = allowed.trim().toLowerCase();
            normalizedAllowed = normalizedAllowed.replaceAll("^(https?://|www\\.)+", "");
            normalizedAllowed = normalizedAllowed.replaceAll("/+$", "");

            String[] parts = normalizedAllowed.split("\\.");
            StringBuilder regexBuilder = new StringBuilder("(?i)(?:https?://)?(?:www\\.)?");

            for (int i = 0; i < parts.length; i++) {
                regexBuilder.append(Pattern.quote(parts[i]));
                if (i < parts.length - 1) {
                    regexBuilder.append(dotRegex);
                }
            }

            regexBuilder.append("(?:/\\S*)?");

            result = result.replaceAll(regexBuilder.toString(), "");
        }
        return result;
    }

    private boolean containsBlacklistedRegex(String text, String[] blacklist) {
        for (String word : blacklist) {
            Pattern p = Pattern.compile(buildLeetspeakRegex(word));
            if (p.matcher(text).find()) return true;
        }
        return false;
    }

    private boolean containsBlacklistedSkeleton(String skeleton, String[] blacklist) {
        for (String word : blacklist) {
            Pattern p = Pattern.compile(buildLeetspeakRegex(word).replace("\\s*", ""));
            if (p.matcher(skeleton).find()) return true;
        }
        return false;
    }

    private boolean containsDomainRegex(String text) {
        String dotRegex = "(\\s*(\\.|dot|punkt|,|_|-)\\s*)";
        for (String domain : DOMAINS) {
            Pattern p = Pattern.compile("[a-z0-9-]{3,}" + dotRegex + domain + "\\b");
            if (p.matcher(text).find()) return true;
        }
        return false;
    }

    private boolean containsDomainSkeleton(String skeleton) {
        for (String domain : DOMAINS) {
            Pattern p = Pattern.compile("[a-z0-9]{3,}(dot|punkt)" + domain);
            if (p.matcher(skeleton).find()) return true;

            if (skeleton.matches(".*[a-z0-9]{4,}" + domain + "$")) return true;
        }
        return false;
    }

    private String buildLeetspeakRegex(String word) {
        StringBuilder regex = new StringBuilder("\\b");
        for (char c : word.toCharArray()) {
            switch (c) {
                case 'a': regex.append("[a4@]"); break;
                case 'e': regex.append("[e3]"); break;
                case 'i': regex.append("[i1!jly]"); break;
                case 'o': regex.append("[o0u]"); break;
                case 's': regex.append("[s5z$]"); break;
                case 't': regex.append("[t7]"); break;
                default: regex.append(c); break;
            }
            regex.append("\\s*");
        }
        regex.append("\\b");
        return regex.toString();
    }
}