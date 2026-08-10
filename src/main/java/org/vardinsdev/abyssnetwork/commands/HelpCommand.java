package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.Messages;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {

                player.sendMessage(Messages.title("Abyss Network: Help"));

                player.sendMessage(""); // Spacer
                player.sendMessage(Messages.info("- '/help' provides information on how each command works"));

                player.sendMessage(""); // Spacer
                player.sendMessage(Messages.info("- '/class' changes your weapon preference."));
            }
        });
    }
}
