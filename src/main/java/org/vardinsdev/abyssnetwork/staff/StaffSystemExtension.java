package org.vardinsdev.abyssnetwork.staff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class StaffSystemExtension {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final File STAFF_FILE = new File("config/staff.json");

    public static void register() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            UUID playerUuid = player.getUuid();

            if (!STAFF_FILE.exists()) return;

            try {
                // Read the staff database
                Map<UUID, StaffMember> staffMap = MAPPER.readValue(
                        STAFF_FILE,
                        new TypeReference<Map<UUID, StaffMember>>() {}
                );

                // Check if they are a staff member
                if (staffMap.containsKey(playerUuid)) {
                    StaffMember staff = staffMap.get(playerUuid);

                    // Assign their rank/permissions to the player instance here
                    System.out.println("[StaffSystem] Loaded " + player.getUsername() + " as " + staff.getRank());
                }
            } catch (Exception e) {
                System.err.println("[StaffSystem] Failed to read staff.json!");
                e.printStackTrace();
            }
        });
    }
}
