package org.vardinsdev.abyssnetwork.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoadedEvent;
import org.vardinsdev.abyssnetwork.AbyssLogger;

public class PlayerLoaded {
    public static void register() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerLoadedEvent.class, event -> {
            AbyssLogger.success(event.getPlayer().getUsername() + " has joined the server!");
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                onlinePlayer.sendMessage(Component.text(event.getPlayer().getUsername() + " has joined the server!").color(NamedTextColor.YELLOW));
            }
        });
    }
}
