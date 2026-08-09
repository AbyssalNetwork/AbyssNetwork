package org.vardinsdev.abyssnetwork.Database;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;

/**
 * Upserts the player row on join so the backend always has a current username.
 * Runs asynchronously - it never blocks the configuration thread.
 */
public class PlayerSync {
    public static void register() {
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            ApiClient api = ApiClient.getInstance();
            if (api.isEnabled()) {
                api.upsertPlayer(event.getPlayer().getUuid().toString(), event.getPlayer().getUsername());
            }
        });
    }
}
