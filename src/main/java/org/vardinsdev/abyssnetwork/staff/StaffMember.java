package org.vardinsdev.abyssnetwork.staff;

import java.util.UUID;

public class StaffMember {
    private UUID uuid;
    private String lastKnownName;
    private String rank;
    private boolean vanished;

    // Empty constructor required by Jackson
    public StaffMember() {}

    public StaffMember(UUID uuid, String lastKnownName, String rank) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.rank = rank;
        this.vanished = false;
    }

    // Getters and Setters
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String lastKnownName) { this.lastKnownName = lastKnownName; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public boolean isVanished() { return vanished; }
    public void setVanished(boolean vanished) { this.vanished = vanished; }
}
