package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import javax.swing.*;

public class HelpCommand extends Command{
    public HelpCommand() {
        super("help");

        addSyntax((sender, context) -> {
            if(sender instanceof Player player) {
                player.sendMessage(Component.text("|| HELP ||" +
                        "  "
                ));
                player.sendMessage(Component.text("/give (weapon name) is used to provide yourself with the defined weapon.").color(NamedTextColor.RED));
                player.sendMessage(Component.text("/test -- LOREM IPSUM").color(NamedTextColor.DARK_AQUA));
            }
        });
    }
}
