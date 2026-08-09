package org.vardinsdev.abyssnetwork.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;

public class PlayerConfiguration {
    public static void register(InstanceContainer instanceContainer) {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var player = event.getPlayer();

            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(-18.5, 34.0, 13.5));

            if (StaffManager.getInstance().isStaff(player.getUuid())) {
                StaffMember staff = StaffManager.getInstance().getStaff(player.getUuid());

                // Automatically pulls the assigned integer permission level directly from the enum
                player.setPermissionLevel(staff.getRank().getPermissionLevel());

                AbyssLogger.info("[StaffSystem] Set " + player.getUsername() + "'s permission level to " + staff.getRank().getPermissionLevel() + " (" + staff.getRank().name() + ")");
            }
        });
    }
}