package org.vardinsdev.abyssnetwork.Database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ban {
    private String uuid;
    private String username;
    private String reason;
    @JsonProperty("bannedBy")
    private String bannedBy;
    @JsonProperty("bannedAt")
    private String bannedAt;

    public Ban() {
    }

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
}
