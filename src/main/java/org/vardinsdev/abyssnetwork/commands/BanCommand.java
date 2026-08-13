package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;

public class BanCommand extends Command {

    public BanCommand() {
        super("ban");

        var playerArgument = ArgumentType.Word("player");
        var reasonArgument = ArgumentType.StringArray("reason");

        addSyntax((sender, context) -> {
            if (!StaffManager.getInstance().hasStaffAccess(sender)) {
                sender.sendMessage(Messages.error("You must be staff to use this command!"));
                return;
            }

            String targetName = context.get(playerArgument);
            String reason = String.join(" ", context.get(reasonArgument));
            String bannedBy = StaffManager.senderName(sender);

            Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            if (online != null) {
                banAndKick(sender, online.getUuid().toString(), online.getUsername(), reason, bannedBy);
                return;
            }

            // Offline target: resolve the UUID from the backend first.
            ApiClient.getInstance().fetchPlayerByUsername(targetName).thenAccept(stats -> {
                if (stats == null || stats.getUuid() == null) {
                    sender.sendMessage(Messages.error("No player found with the name '" + targetName + "'."));
                    return;
                }
                ApiClient.getInstance()
                        .banPlayer(stats.getUuid(), stats.getUsername(), reason, bannedBy)
                        .thenRun(() -> {
                            sender.sendMessage(Messages.system("Banned " + targetName + "."));
                            AbyssLogger.warn(StaffManager.senderName(sender) + " banned " + targetName + " (" + reason + ")");
                        });
            });
        }, playerArgument, reasonArgument);
    }

    private void banAndKick(net.minestom.server.command.CommandSender sender, String uuid, String username,
                            String reason, String bannedBy) {
        ApiClient.getInstance().banPlayer(uuid, username, reason, bannedBy).thenRun(() -> {
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(username);
            if (target != null) {
                target.kick(Messages.system("You have been banned from Abyss Network.", reason));
            }
            sender.sendMessage(Messages.system("Banned " + username + "."));
            AbyssLogger.warn(StaffManager.senderName(sender) + " banned " + username + " (" + reason + ")");
        });
    }
}
