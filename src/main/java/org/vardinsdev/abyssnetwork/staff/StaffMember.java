package org.vardinsdev.abyssnetwork.staff;

import java.util.UUID;

public class StaffMember {
    private UUID uuid;
    private String lastKnownName;
    private StaffRank rank; // Changed from String to StaffRank
    private boolean vanished;

    public StaffMember() {}

    public StaffMember(UUID uuid, String lastKnownName, StaffRank rank) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.rank = rank;
        this.vanished = false;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String lastKnownName) { this.lastKnownName = lastKnownName; }

    public StaffRank getRank() { return rank; }
    public void setRank(StaffRank rank) { this.rank = rank; }

    public boolean isVanished() { return vanished; }
    public void setVanished(boolean vanished) { this.vanished = vanished; }
}