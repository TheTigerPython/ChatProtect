package de.thetigerpython.chatprotect.checks;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class Buffer {

    public HashMap<Player, Double> buffer = new HashMap<>();

    public double increase(Player player) {
        buffer.put(player, buffer.getOrDefault(player, 0.0) + 1);
        return buffer.get(player);
    }

    public double decrease(Player player) {
        return decrease(player, 1);
    }

    public double decrease(Player player, double value)  {
        buffer.put(player, Math.max(buffer.getOrDefault(player, 0.0) - value, 0));
        return buffer.get(player);
    }

    public double get(Player player) {
        return buffer.get(player);
    }

}