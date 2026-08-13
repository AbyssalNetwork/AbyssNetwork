package org.vardinsdev.abyssnetwork.staff;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Database.ApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory staff cache with write-through persistence to the Abyss Go backend.
 * Reads happen synchronously from the cache (fast, join thread safe); every
 * mutation is pushed to the API asynchronously. The old JSON-file based
 * storage (config/staff.json) is gone.
 */
public class StaffManager {
    private static final StaffManager INSTANCE = new StaffManager();
    public static StaffManager getInstance() { return INSTANCE; }

    private final Map<UUID, StaffMember> staffCache = new HashMap<>();

    public void loadStaff() {
        ApiClient api = ApiClient.getInstance();
        if (!api.isEnabled()) {
            AbyssLogger.warn("Skipping staff load: API client disabled (TYPE=dev).");
            return;
        }
        api.fetchStaff()
                .thenAccept(this::loadAll)
                .exceptionally(ex -> {
                    AbyssLogger.error("Failed to load staff from API: " + ex.getMessage());
                    return null;
                });
    }

    private void loadAll(List<StaffMember> staff) {
        staffCache.clear();
        for (StaffMember member : staff) {
            staffCache.put(member.getUuid(), member);
        }
        AbyssLogger.success("Loaded " + staff.size() + " staff members from API.");
    }

    public boolean isStaff(UUID uuid) {
        return staffCache.containsKey(uuid);
    }

    /** Whether a command sender may use staff commands: the console, or a registered staff member. */
    public boolean hasStaffAccess(CommandSender sender) {
        return sender instanceof ConsoleSender || isStaff(sender.identity().uuid());
    }

    /** Human-readable name for the actor behind a command: player username, or {@code CONSOLE}. */
    public static String senderName(CommandSender sender) {
        return sender instanceof Player player ? player.getUsername() : "CONSOLE";
    }

    public StaffMember getStaff(UUID uuid) {
        return staffCache.get(uuid);
    }

    public void addStaff(StaffMember member) {
        staffCache.put(member.getUuid(), member);
        writeThrough(member);
    }

    public void updateStaff(StaffMember member) {
        staffCache.put(member.getUuid(), member);
        writeThrough(member);
    }

    public void removeStaff(UUID uuid) {
        staffCache.remove(uuid);
        ApiClient.getInstance().deleteStaff(uuid);
    }

    private void writeThrough(StaffMember member) {
        ApiClient.getInstance().upsertStaff(member)
                .exceptionally(ex -> {
                    AbyssLogger.error("Failed to persist staff " + member.getUuid() + ": " + ex.getMessage());
                    return null;
                });
    }
}
