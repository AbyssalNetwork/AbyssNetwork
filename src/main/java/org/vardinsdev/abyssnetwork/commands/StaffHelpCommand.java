package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class StaffHelpCommand extends Command{
    public StaffHelpCommand() {
        super("staff-help");

        addSyntax(((sender, context) -> {
            if(sender instanceof Player player) {
                if(player.getPermissionLevel() >= 2) {
                    player.sendMessage(Component.text("AbyssNetwork: Staff Information").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));

                    player.sendMessage(""); // Spacer

                    player.sendMessage(Component.text("To use staff chat, please add a % sign in front of your message. Add a space proceeding the % sign to ensure the message is not sent publicly, as the syntax requirement is specific and will break without a space.").color(NamedTextColor.AQUA));

                    player.sendMessage(""); // Spacer

                    player.sendMessage(Component.text("- '/give' (weapon name) is used to provide yourself with the defined weapon.").color(NamedTextColor.AQUA));
                }
            }
        }));
    }
}