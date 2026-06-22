package org.vardinsdev.abyssnetwork.commands;

import org.vardinsdev.minegun.Weapons.Rifle;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import org.vardinsdev.minegun.Weapons.RocketLauncher;
import org.vardinsdev.minegun.minegunLogger;

public class GiveCommand extends Command{

    public GiveCommand() {
        super("give");

        var item = ArgumentType.Word("item-given")
                .from("RocketLauncher", "Rifle"); // Auto Fill

        addSyntax((sender, context) -> {
            if (sender instanceof Player p) {
                if (p.getPermissionLevel() >= 2) {
                    if (context.get(item).equalsIgnoreCase("rocketLauncher")) {
                        RocketLauncher.givePlayer(p);
                    }
                    if (context.get(item).equalsIgnoreCase("rifle")) {
                        Rifle.givePlayer(p);
                    }
                }
            } else {
                minegunLogger.error("Console tried to give itself a rocket launcher!");
            }
        }, item);
    }
}