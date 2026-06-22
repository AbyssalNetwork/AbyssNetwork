package org.vardinsdev.abyssnetwork.events;

import io.github.cdimascio.dotenv.Dotenv;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.envHandler;

import java.util.Objects;

public class PlayerConfiguration {
    public static void register(InstanceContainer instanceContainer) {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(-18.5, 34.0, 13.5));
            Dotenv env = envHandler.register();
            if (Objects.equals(env.get("TYPE"), "dev")) {
                event.getPlayer().setPermissionLevel(4);
                AbyssLogger.info(event.getPlayer().getUsername() + " has been given permission level 4 because of dev mode");
            }
        });
    }
}
