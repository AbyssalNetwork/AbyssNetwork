package org.vardinsdev.abyssnetwork;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Centralised styling for all player-facing messages.
 *
 * <p>Palette: titles are bold dark purple, body/info text is aqua, errors are
 * red, and broadcast announcements (e.g. joins) are yellow.
 */
public final class Messages {

    private Messages() {
    }

    /** A bold dark-purple section header. */
    public static Component title(String text) {
        return Component.text(text).color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD);
    }

    /** Standard aqua body text. */
    public static Component info(String text) {
        return Component.text(text).color(NamedTextColor.AQUA);
    }

    /** Red error text. */
    public static Component error(String text) {
        return Component.text(text).color(NamedTextColor.RED);
    }

    /** Yellow announcement text (e.g. join/leave messages). */
    public static Component announce(String text) {
        return Component.text(text).color(NamedTextColor.YELLOW);
    }

    /** The standard "Abyss Network System" header followed by an info line. */
    public static Component system(String info) {
        return system("Abyss Network System", info);
    }

    /** A custom header followed by an info line. */
    public static Component system(String title, String info) {
        return Component.text()
                .append(title(title))
                .append(Component.newline())
                .append(info(info))
                .build();
    }
}
