package de.thetigerpython.chatprotect.checks;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
public abstract class Check {

    private final String simpleName;

    private boolean enabled;

    private List<String> punishments = new ArrayList<>();

    protected final Buffer buffer = new Buffer();

    private HashMap<UUID, Integer> violations = new HashMap<>();

    public Check(String simpleName) {
        this.simpleName = simpleName;
        reloadConfig();
    }

    public abstract CheckResult checkMessage(Player player, String string);

    public abstract void reloadConfig();

    public String simpleCleanUp(String input) {
        return input.replaceAll("[_:\\-,;()§$%&/=*#'^°|]", "").toLowerCase();
    }

    public List<String> cleanUp(String input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }

        if (input.length() > 64 || input.matches("^[0-9\\s-_]+$")) {
            return List.of(input);
        }

        String base = input
                .replaceAll("[_:\\-,;()§$%&/=*#'^°|]", "")
                .toLowerCase();

        StringBuilder normalized = new StringBuilder(base.length());
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            char mapped = switch (c) {
                case '0', 'о' -> 'o';
                case '3', 'е' -> 'e';
                case '4', '@', 'а' -> 'a';
                case '8' -> 'b';
                case '1', '!', '|', 'і' -> 'i';
                case '5', '$', 'z', 'с' -> 's';
                case '7', '+' -> 't';
                case '9', '6' -> 'g';
                case '2' -> 'z';
                case 'р' -> 'p';
                case 'у' -> 'y';
                case 'х' -> 'x';
                default -> c;
            };
            normalized.append(mapped);
        }

        String normalizedStr = normalized.toString();
        String collapsed = normalizedStr.replaceAll("(.)\\1+", "$1");

        List<String> results = new ArrayList<>(4);
        results.add(input);
        results.add(base);
        results.add(normalizedStr);
        results.add(collapsed);

        return results;
    }

}