package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
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
                        player.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                        player.sendMessage(Component.text("You have received a 'Rifle'.").color(NamedTextColor.AQUA));
                    }
                    if (context.get(item).equalsIgnoreCase("rocketlauncher")) {
                        RocketLauncher.givePlayer(player);
                        player.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                        player.sendMessage(Component.text("You have received a 'Rocket Launcher'.").color(NamedTextColor.AQUA));
                    }
                }
            } else {
                AbyssLogger.error("Console tried to give itself a rocket launcher!");
            }
        }, item);
    }
}