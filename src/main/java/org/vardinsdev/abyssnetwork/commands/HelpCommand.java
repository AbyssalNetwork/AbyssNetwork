package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class HelpCommand extends Command{
    public HelpCommand() {
        super("help");

        addSyntax((sender, context) -> {
            if(sender instanceof Player player) {
                player.sendMessage("/give (weapon name) is used to provide yourself with the defined weapon.");
            }
        });
    }
}
