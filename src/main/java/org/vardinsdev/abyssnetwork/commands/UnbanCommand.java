package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban");

        var playerArgument = ArgumentType.Word("player");

        addSyntax((sender, context) -> {
            if (!StaffManager.getInstance().isStaff(sender.identity().uuid())) {
                sender.sendMessage(Messages.error("You must be staff to use this command!"));
                return;
            }

            String targetName = context.get(playerArgument);

            ApiClient.getInstance().fetchPlayerByUsername(targetName).thenAccept(stats -> {
                if (stats == null || stats.getUuid() == null) {
                    sender.sendMessage(Messages.error("No player found with the name '" + targetName + "'."));
                    return;
                }
                ApiClient.getInstance().unban(stats.getUuid()).thenRun(() -> {
                    sender.sendMessage(Messages.system("Unbanned " + targetName + "."));
                    AbyssLogger.warn(sender + " unbanned " + targetName);
                });
            });
        }, playerArgument);
    }
}
