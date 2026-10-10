package de.thetigerpython.chatprotect.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorFormatter {

    public static String translateHexColorCodes(String message) {
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.valueOf("#" + color).toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}

