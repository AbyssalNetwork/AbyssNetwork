package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.abyssnetwork.Database.BanHistory;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;

import java.util.List;

public class BanLogCommand extends Command {

    private static final int MAX_ENTRIES = 20;

    public BanLogCommand() {
        super("ban-log");

        var pageArgument = ArgumentType.Integer("page").setDefaultValue(1);

        addSyntax((sender, context) -> {
            if (!StaffManager.getInstance().hasStaffAccess(sender)) {
                sender.sendMessage(Messages.error("You must be staff to use this command!"));
                return;
            }

            int page = Math.max(1, context.get(pageArgument));
            ApiClient.getInstance().fetchBanHistory().thenAccept(history -> {
                if (history.isEmpty()) {
                    sender.sendMessage(Messages.system("Ban Log", "No ban records yet."));
                    return;
                }

                int totalPages = (int) Math.ceil(history.size() / (double) MAX_ENTRIES);
                int effectivePage = Math.min(page, totalPages);

                int from = (effectivePage - 1) * MAX_ENTRIES;
                int to = Math.min(from + MAX_ENTRIES, history.size());

                sender.sendMessage(Messages.title("Abyss Network: Ban Log (page " + effectivePage + "/" + totalPages + ")"));
                for (BanHistory h : history.subList(from, to)) {
                    String line = "#" + h.getId() + " " + h.getUsername()
                            + " | " + h.getReason()
                            + " | by " + (h.getBannedBy() == null ? "?" : h.getBannedBy())
                            + " at " + h.getBannedAt();
                    if (h.isActive()) {
                        line += " | ACTIVE";
                    } else {
                        line += " | unbanned by " + (h.getUnbannedBy() == null ? "?" : h.getUnbannedBy());
                    }
                    sender.sendMessage(Messages.info(line));
                }
                sender.sendMessage(Messages.info("Use /ban-log <page> to see more."));
            });
        }, pageArgument);
    }
}
