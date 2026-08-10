package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;
import org.vardinsdev.abyssnetwork.staff.StaffRank;

import java.util.Arrays;
import java.util.UUID;

public class GiveRankCommand extends Command {

    public GiveRankCommand() {
        super("giverank");

        // Define arguments: <player name> <rank name>
        var playerArgument = ArgumentType.Word("targetPlayer");
        var rankArgument = ArgumentType.Word("rank");

        // Set command syntax
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArgument);
            String rankName = context.get(rankArgument).toUpperCase();

            if (!StaffManager.getInstance().isStaff(sender.identity().uuid())) {
                sender.sendMessage(Messages.error("You must be staff to give a rank!"));
                return;
            }

            // Resolve the target player's UUID (online-only in this implementation)
            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (targetPlayer == null) {
                sender.sendMessage(Messages.error("Player must be online to assign a rank via this simple command example!"));
                return;
            }

            StaffRank rank;
            try {
                rank = StaffRank.valueOf(rankName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Messages.error("Unknown rank '" + rankName + "'. Valid ranks: " + Arrays.toString(StaffRank.values())));
                return;
            }

            if (sender instanceof Player player) {
                if (StaffManager.getInstance().getStaff(player.getUuid()).getRank().ordinal() <= rank.ordinal()) {
                    sender.sendMessage(Messages.error("You must be a higher rank than " + rankName + " to give out this rank!"));
                    return;
                }
            }

            UUID uuid = targetPlayer.getUuid();
            StaffMember staff = StaffManager.getInstance().getStaff(uuid);
            if (staff == null) {
                staff = new StaffMember(uuid, targetName, rank);
            } else {
                staff.setLastKnownName(targetName);
                staff.setRank(rank);
            }
            StaffManager.getInstance().addStaff(staff);

            // Apply the new permission level immediately instead of waiting for a rejoin.
            targetPlayer.setPermissionLevel(rank.getPermissionLevel());

            sender.sendMessage(Messages.system("Successfully set " + targetName + "'s rank to " + rankName));

            targetPlayer.sendMessage(Messages.system("Your staff rank has been updated to " + rankName));

            AbyssLogger.warn(sender + " has given " + targetName + " the rank of " + rankName);

        }, playerArgument, rankArgument);
    }
}
