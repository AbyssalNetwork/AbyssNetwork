package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick");

        var playerArgument = ArgumentType.Word("player");
        var reasonArgument = ArgumentType.StringArray("reason").setDefaultValue(new String[]{"No reason provided"});

        addSyntax((sender, context) -> {
            if (!StaffManager.getInstance().isStaff(sender.identity().uuid())) {
                sender.sendMessage(Messages.error("You must be staff to use this command!"));
                return;
            }

            String targetName = context.get(playerArgument);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            if (target == null) {
                sender.sendMessage(Messages.error("Player '" + targetName + "' is not online!"));
                return;
            }

            String reason = String.join(" ", context.get(reasonArgument));
            target.kick(Messages.system("You have been kicked from Abyss Network.", reason));
            sender.sendMessage(Messages.system("Kicked " + target.getUsername() + "."));
            AbyssLogger.warn(sender + " kicked " + target.getUsername() + " (" + reason + ")");

        }, playerArgument, reasonArgument);
    }
}
