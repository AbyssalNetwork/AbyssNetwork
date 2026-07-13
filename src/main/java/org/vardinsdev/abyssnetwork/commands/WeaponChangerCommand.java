package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
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
                        player.sendMessage(Component.text("You have changed your weapon preference to 'Rifle'.").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                    }
                    if (context.get(item).equalsIgnoreCase("rocketLauncher")) {
                        player.getInventory().clear();
                        RocketLauncher.givePlayer(player);
                        player.sendMessage(Component.text("You have changed your weapon preference to 'Rocket Launcher'.").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                    }
        }
                });
}}