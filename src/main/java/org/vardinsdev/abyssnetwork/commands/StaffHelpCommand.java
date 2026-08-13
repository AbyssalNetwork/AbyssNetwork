package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.Messages;

public class StaffHelpCommand extends Command {
    public StaffHelpCommand() {
        super("staff-help");

        addSyntax(((sender, context) -> {
            if(sender instanceof Player player) {
                if(player.getPermissionLevel() >= 2) {

                    player.sendMessage(Messages.title("Abyss Network: Staff Information"));

                    player.sendMessage(""); // Spacer
                    player.sendMessage(Messages.info("- To use staff chat: %text"));

                    player.sendMessage(""); // Spacer
                    player.sendMessage(Messages.info("- '/give' (weapon name) is used to provide yourself with the defined weapon."));
                    player.sendMessage(Messages.info("- '/kick' (player) [reason] kicks an online player."));
                    player.sendMessage(Messages.info("- '/ban' (player) [reason] bans an online or offline player."));
                    player.sendMessage(Messages.info("- '/unban' (player) unbans a banned player."));
                    player.sendMessage(Messages.info("- '/ban-log' [page] lists the ban history."));
                    player.sendMessage(Messages.info("- '/vanish' hides you from non-staff players."));
                    player.sendMessage(Messages.info("- '/giverank' (player) (rank) assigns a staff rank."));
                    player.sendMessage(Messages.info("- '/fire' (player) removes a player's staff rank."));
                } else {
                    player.sendMessage(Messages.system("You must be a staff member to use this command!"));
                }
            }
        }));
    }
}