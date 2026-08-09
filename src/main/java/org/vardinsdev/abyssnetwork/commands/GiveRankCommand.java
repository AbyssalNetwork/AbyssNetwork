package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
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
                sender.sendMessage("You must be staff to give a rank!");
                return;
            }

            // Resolve the target player's UUID (online-only in this implementation)
            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (targetPlayer == null) {
                sender.sendMessage("Player must be online to assign a rank via this simple command example!");
                return;
            }

            StaffRank rank;
            try {
                rank = StaffRank.valueOf(rankName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Unknown rank '" + rankName + "'. Valid ranks: " + Arrays.toString(StaffRank.values()));
                return;
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

            sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text("Successfully set " + targetName + "'s rank to " + rankName).color(NamedTextColor.AQUA));

            targetPlayer.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            targetPlayer.sendMessage(Component.text("Your staff rank has been updated to " + rankName).color(NamedTextColor.AQUA));

            AbyssLogger.warn(sender + " has given " + targetName + " the rank of " + rankName);

        }, playerArgument, rankArgument);
    }
}
