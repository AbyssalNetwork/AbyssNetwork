package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.minegun.Weapons.Rifle;
import org.vardinsdev.minegun.Weapons.RocketLauncher;

public class WeaponChangerCommand extends Command {
    public WeaponChangerCommand() {
        super("class");

        var item = ArgumentType.Word("item-given")
                .from("Rifle", "RocketLauncher"); // Auto Fill

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                if (context.get(item).equalsIgnoreCase("rifle")) {
                    player.getInventory().clear();
                    Rifle.givePlayer(player);

                    player.sendMessage(Messages.system("You have changed your weapon preference to 'Rifle'."));
                }
                if (context.get(item).equalsIgnoreCase("rocketlauncher")) {
                    player.getInventory().clear();
                    RocketLauncher.givePlayer(player);

                    player.sendMessage(Messages.system("You have changed your weapon preference to 'Rocket Launcher'."));
                }
            }
        }, item);
    }
}