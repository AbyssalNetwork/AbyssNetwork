package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.minegun.Weapons.Rifle;
import org.vardinsdev.minegun.Weapons.RocketLauncher;

public class GiveCommand extends Command {

    public GiveCommand() {
        super("give");

        var item = ArgumentType.Word("item-given")
                .from("Rifle", "RocketLauncher"); // Auto Fill

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                if (player.getPermissionLevel() >= 2) {
                    if (context.get(item).equalsIgnoreCase("rifle")) {
                        Rifle.givePlayer(player);
                        player.sendMessage(Messages.system("You have received a 'Rifle'."));
                    }
                    if (context.get(item).equalsIgnoreCase("rocketlauncher")) {
                        RocketLauncher.givePlayer(player);
                        player.sendMessage(Messages.system("You have received a 'Rocket Launcher'."));
                    }
                }
            } else {
                AbyssLogger.error("Console tried to give itself a rocket launcher!");
            }
        }, item);
    }
}