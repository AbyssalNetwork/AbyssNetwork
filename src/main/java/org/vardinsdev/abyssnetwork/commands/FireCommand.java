package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;

import java.util.UUID;

public class FireCommand extends Command {

    public FireCommand() {
        super("fire");

        // Define argument: <player name>
        var playerArgument = ArgumentType.Word("targetPlayer");

        addSyntax((sender, context) -> {
            String targetName = context.get(playerArgument);

            // Look up the target player if they are online
            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (targetPlayer == null) {
                sender.sendMessage(Messages.system("AbyssNetwork Staff System", "Player must be online to fire them via this command!"));
                return;
            }

            UUID uuid = targetPlayer.getUuid();

            // 1. Check if the target is actually part of the staff team
            if (!StaffManager.getInstance().isStaff(uuid)) {
                sender.sendMessage(Messages.system("AbyssNetwork Staff System", targetName + " is not a registered staff member."));
                return;
            }

            StaffMember staff = StaffManager.getInstance().getStaff(uuid);
            String oldRankName = staff.getRank().name();

            // 2. Remove them entirely from the staff cache map and update live privileges
            StaffManager.getInstance().removeStaff(uuid);
            targetPlayer.setPermissionLevel(0);

            // 3. Notify the executor and the target player
            sender.sendMessage(Messages.system("Successfully fired " + targetName + " (removed from rank " + oldRankName + ")."));

            targetPlayer.sendMessage(Messages.system("You have been relieved of your duties and removed from the staff team."));

            // Log the action
            AbyssLogger.warn("[StaffSystem] " + sender.identity().uuid() + " FIRED " + targetName + " (was " + oldRankName + ")");
        }, playerArgument);
    }
}