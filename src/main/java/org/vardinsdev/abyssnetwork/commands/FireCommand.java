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
                sender.sendMessage(Component.text("AbyssNetwork Staff System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("Player must be online to fire them via this command!").color(NamedTextColor.AQUA));
                return;
            }

            UUID uuid = targetPlayer.getUuid();

            // 1. Check if the target is actually part of the staff team
            if (!StaffManager.getInstance().isStaff(uuid)) {
                sender.sendMessage(Component.text("AbyssNetwork Staff System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text(targetName + " is not a registered staff member.").color(NamedTextColor.AQUA));
                return;
            }

            StaffMember staff = StaffManager.getInstance().getStaff(uuid);
            String oldRankName = staff.getRank().name();

            // 2. Remove them entirely from the staff cache map and update live privileges
            StaffManager.getInstance().removeStaff(uuid);
            targetPlayer.setPermissionLevel(0);

            // 3. Notify the executor and the target player
            sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text("Successfully fired " + targetName + " (removed from rank " + oldRankName + ".").color(NamedTextColor.AQUA));

            targetPlayer.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            targetPlayer.sendMessage(Component.text("You have been relieved of your duties and removed from the staff team.").color(NamedTextColor.AQUA));

            // Log the action
            AbyssLogger.warn("[StaffSystem] " + sender.identity().identity().identity().identity().toString() + " FIRED " + targetName + " (was " + oldRankName + ")");


        }, playerArgument);
    }
}