package org.vardinsdev.abyssnetwork.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.minegun.HealthManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks kills and deaths and persists them to the Abyss backend.
 *
 * <p>Combat is MineGun-based: weapons use a custom health tag and never call
 * {@code Player.kill()}, so {@link PlayerDeathEvent} does not fire for weapon
 * kills. Those deaths are detected by watching the custom health tag drop to
 * zero, with the killer read from MineGun's public {@code killedBy} map.
 * Vanilla deaths (fall, lava, vanilla PvP, etc.) are handled via
 * {@link PlayerDeathEvent}, crediting the last player who damaged the victim.
 *
 * <p>MUST be registered before {@code HealthManagement.register()}: listeners
 * run in registration order, and MineGun resets the health tag back to 100
 * during the same tick the death is detected.
 */
public class KillTracker {
    /** Windows for de-duplicating a weapon kill against a simultaneous vanilla death. */
    private static final long DEDUP_WINDOW_MS = 3000;

    private static final Map<UUID, Double> lastHealth = new HashMap<>();
    private static final Map<UUID, UUID> lastHitBy = new HashMap<>();
    private static final Map<UUID, Long> lastVanillaDeath = new HashMap<>();

    public static void register() {
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        // Remember who last damaged each player, for vanilla PvP kill credit.
        handler.addListener(EntityDamageEvent.class, event -> {
            if (event.getEntity() instanceof Player victim) {
                if (event.getDamage().getAttacker() instanceof Player killer) {
                    lastHitBy.put(victim.getUuid(), killer.getUuid());
                }
            }
        });

        // Vanilla deaths (fall, lava, suffocation, vanilla PvP, ...).
        handler.addListener(PlayerDeathEvent.class, event -> {
            Player victim = event.getPlayer();
            UUID victimUuid = victim.getUuid();
            lastVanillaDeath.put(victimUuid, System.currentTimeMillis());
            increment(victimUuid, victim.getUsername(), 0, 1);

            UUID killerUuid = lastHitBy.remove(victimUuid);
            if (killerUuid != null) {
                Player killer = getOnlinePlayer(killerUuid);
                if (killer != null) {
                    increment(killer.getUuid(), killer.getUsername(), 1, 0);
                }
            }
        });

        // MineGun custom-health deaths.
        handler.addListener(PlayerTickEvent.class, event -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUuid();
            Double health = player.getTag(HealthManagement.healthTag);
            if (health == null) {
                lastHealth.remove(uuid);
                return;
            }
            Double prev = lastHealth.put(uuid, health);
            if (prev == null || prev <= 0 || health > 0) return;

            // A simultaneous vanilla death was already counted - don't double count.
            long lastVanilla = lastVanillaDeath.getOrDefault(uuid, 0L);
            if (System.currentTimeMillis() - lastVanilla < DEDUP_WINDOW_MS) {
                return;
            }
            lastVanillaDeath.remove(uuid);

            increment(uuid, player.getUsername(), 0, 1);
            Player killer = HealthManagement.killedBy.remove(uuid);
            if (killer != null) {
                increment(killer.getUuid(), killer.getUsername(), 1, 0);
            }
        });

        // Clean up per-session tracking.
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            UUID uuid = event.getPlayer().getUuid();
            lastHealth.remove(uuid);
            lastHitBy.remove(uuid);
            lastVanillaDeath.remove(uuid);
            HealthManagement.killedBy.remove(uuid);
        });
    }

    private static void increment(UUID uuid, String username, int kills, int deaths) {
        if (username == null) return;
        ApiClient.getInstance().recordStats(uuid.toString(), username, kills, deaths);
    }

    private static Player getOnlinePlayer(UUID uuid) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player.getUuid().equals(uuid)) return player;
        }
        return null;
    }
}
