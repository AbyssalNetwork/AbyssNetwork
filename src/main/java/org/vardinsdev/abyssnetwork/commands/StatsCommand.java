package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.Database.ApiClient;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats");

        var nameArgument = ArgumentType.Word("player");

        addSyntax((sender, context) -> {
            show(sender, context.get(nameArgument));
        }, nameArgument);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Usage: /stats <player>").color(NamedTextColor.RED));
                return;
            }
            show(sender, player.getUsername());
        });
    }

    private static void show(CommandSender sender, String name) {
        ApiClient api = ApiClient.getInstance();
        if (!api.isEnabled()) {
            sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text("Stats are unavailable in dev mode.").color(NamedTextColor.AQUA));
            return;
        }

        Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
        var future = online != null
                ? api.fetchPlayer(online.getUuid().toString())
                : api.fetchPlayerByUsername(name);

        future.thenAccept(stats -> {
            if (stats == null) {
                sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("No stats found for '" + name + "'.").color(NamedTextColor.AQUA));
                return;
            }
            String kd = stats.getDeaths() > 0
                    ? String.format("%.2f", stats.getKills() / (double) stats.getDeaths())
                    : (stats.getKills() > 0 ? "inf" : "—");
            sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text(stats.getUsername() + "'s Stats").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text("Kills: " + stats.getKills() + "  Deaths: " + stats.getDeaths() + "  K/D: " + kd).color(NamedTextColor.AQUA));
        });
    }
}
