package org.vardinsdev.abyssnetwork.models;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Rank {
    // These match exactly what you put in your MariaDB ENUM
    FOUNDER("Founder", "[Founder] ", NamedTextColor.DARK_RED),
    CO_FOUNDER("Co-Founder", "[Co-Founder] ", NamedTextColor.RED),
    LEADERSHIP("Leadership", "[Leadership] ", NamedTextColor.GOLD),
    DEVELOPER("Developer", "[Dev] ", NamedTextColor.AQUA),
    MEMBER("Member", "", NamedTextColor.GRAY);

    private final String name;
    private final String prefix;
    private final TextColor color;

    Rank(String name, String prefix, TextColor color) {
        this.name = name;
        this.prefix = prefix;
        this.color = color;
    }

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public TextColor getColor() { return color; }

    // This converts the String from your Database into this Java Object
    public static Rank fromString(String text) {
        for (Rank r : Rank.values()) {
            if (r.name.equalsIgnoreCase(text)) return r;
        }
        return MEMBER; // Safety fallback
    }
}