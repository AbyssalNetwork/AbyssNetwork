package org.vardinsdev.abyssnetwork.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import org.vardinsdev.abyssnetwork.AbyssLogger;

public class GamemodeSwitcherEvent {
    public static void register() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerGameModeRequestEvent.class, event -> {
            Player player = event.getPlayer();
            AbyssLogger.info(player.getUsername() + " has attempted to change gamemode!");
            if (player.getPermissionLevel() >= 2) {
                player.setGameMode(event.getRequestedGameMode());
                AbyssLogger.info(player.getUsername() + " has changed gamemode to " + event.getRequestedGameMode());
            }
        });
    }
}
