package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {

                player.sendMessage(Component.text("Abyss Network: Help").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));

                player.sendMessage(""); // Spacer
                player.sendMessage(Component.text("- '/help' provides information on how each command works").color(NamedTextColor.AQUA));

                player.sendMessage(""); // Spacer
                player.sendMessage(Component.text("- '/class' changes your weapon preference.").color(NamedTextColor.AQUA));
            }
        });
    }
}
