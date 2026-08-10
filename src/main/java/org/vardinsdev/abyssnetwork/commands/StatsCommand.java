package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.abyssnetwork.Database.PlayerStats;
import org.vardinsdev.abyssnetwork.Messages;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats");

        var nameArgument = ArgumentType.Word("player");

        addSyntax((sender, context) -> {
            show(sender, context.get(nameArgument));
        }, nameArgument);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Messages.error("Usage: /stats <player>"));
                return;
            }
            show(sender, player.getUsername());
        });
    }

    private static void show(CommandSender sender, String name) {
        ApiClient api = ApiClient.getInstance();
        if (!api.isEnabled()) {
            sender.sendMessage(Messages.system("Stats are unavailable in dev mode."));
            return;
        }

        Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
        var future = online != null
                ? api.fetchPlayer(online.getUuid().toString())
                : api.fetchPlayerByUsername(name);

        future.thenAccept(stats -> {
            if (stats == null) {
                sender.sendMessage(Messages.system("No stats found for '" + name + "'."));
                return;
            }
            String kd = stats.getDeaths() > 0
                    ? String.format("%.2f", stats.getKills() / (double) stats.getDeaths())
                    : (stats.getKills() > 0 ? "inf" : "—");
            sender.sendMessage(Messages.title(stats.getUsername() + "'s Stats"));
            sender.sendMessage(Messages.info("Kills: " + stats.getKills() + "  Deaths: " + stats.getDeaths() + "  K/D: " + kd));
        });
    }
}
