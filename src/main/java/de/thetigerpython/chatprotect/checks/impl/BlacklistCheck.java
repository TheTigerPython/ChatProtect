package de.thetigerpython.chatprotect.checks.impl;

import de.thetigerpython.chatprotect.checks.Check;
import de.thetigerpython.chatprotect.checks.CheckResult;
import de.thetigerpython.chatprotect.utils.services.BlacklistService;
import de.thetigerpython.chatprotect.utils.Config;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlacklistCheck extends Check {

    public BlacklistCheck() {
        super("Toxicity");
    }

    @Override
    public CheckResult checkMessage(Player player, String string) {
        if (!isEnabled()) return new CheckResult();

        CheckResult result = new CheckResult();
        List<String> blacklistedWords = BlacklistService.getBlacklistedWords();

        String simplified = string.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();

        for (String blacklistedWord : blacklistedWords) {
            String target = blacklistedWord.toLowerCase();

            String regex = "\\b" + target.chars()
                    .mapToObj(c -> Pattern.quote(String.valueOf((char) c)))
                    .collect(Collectors.joining("\\s*")) + "\\b";

            Pattern pattern = Pattern.compile(regex);

            if (pattern.matcher(simplified).find()) {
                return result.fail("Blacklist violation: " + target + " (see blacklist.yml)", Config.TOXICITY_MESSAGE);
            }

            for (String cleanOption : cleanUp(simplified)) {
                if (pattern.matcher(cleanOption).find() && Config.TOXICITY_DETECT_LEETSPEAK) {
                    return result.fail("Blacklist violation (leetspeak): " + target + " (see blacklist.yml)", Config.TOXICITY_MESSAGE);
                }
            }
        }

        return result;
    }

    @Override
    public void reloadConfig() {
        setEnabled(Config.TOXICITY_ENABLED);
        setPunishments(Config.TOXICITY_PUNISHMENTS);
    }

}