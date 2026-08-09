package org.vardinsdev.abyssnetwork.Database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStats {
    private String uuid;
    private String username;
    private int kills;
    private int deaths;
    private int team;
    @JsonProperty("player_rank")
    private String playerRank;
    @JsonProperty("is_opped")
    private boolean isOpped;

    public PlayerStats() {
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }

    public String getPlayerRank() { return playerRank; }
    public void setPlayerRank(String playerRank) { this.playerRank = playerRank; }

    public boolean isOpped() { return isOpped; }
    public void setOpped(boolean opped) { isOpped = opped; }
}
