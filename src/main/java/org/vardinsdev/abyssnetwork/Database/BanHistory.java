package org.vardinsdev.abyssnetwork.Database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BanHistory {
    private long id;
    private String uuid;
    private String username;
    private String reason;
    private String bannedBy;
    private String bannedAt;
    private String unbannedBy;
    private String unbannedAt;

    public BanHistory() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getBannedBy() { return bannedBy; }
    public void setBannedBy(String bannedBy) { this.bannedBy = bannedBy; }

    public String getBannedAt() { return bannedAt; }
    public void setBannedAt(String bannedAt) { this.bannedAt = bannedAt; }

    public String getUnbannedBy() { return unbannedBy; }
    public void setUnbannedBy(String unbannedBy) { this.unbannedBy = unbannedBy; }

    public String getUnbannedAt() { return unbannedAt; }
    public void setUnbannedAt(String unbannedAt) { this.unbannedAt = unbannedAt; }

    public boolean isActive() { return unbannedAt == null; }
}
