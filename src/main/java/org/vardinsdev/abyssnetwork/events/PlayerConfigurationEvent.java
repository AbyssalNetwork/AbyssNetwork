package org.vardinsdev.abyssnetwork.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;

public class PlayerConfigurationEvent {
    public static void register(InstanceContainer instanceContainer, GlobalEventHandler eventHandler) {
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(-18.5, 34.0, 13.5));
        });
    }
}
