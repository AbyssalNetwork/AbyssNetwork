package org.vardinsdev.abyssnetwork.staff;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.vardinsdev.abyssnetwork.Messages;

import java.util.UUID;

public class StaffSystemExtension {

    /**
     * Applies the vanish viewable rule to the given (currently vanished) player.
     * Must be re-applied on spawn because each reconnect creates a new Player
     * entity that loses the previous instance's rule.
     */
    public static void applyVanishRule(Player vanishedPlayer) {
        vanishedPlayer.updateViewableRule(viewer -> {
            if (viewer == vanishedPlayer) return true; // self always visible
            StaffMember vanishing = StaffManager.getInstance().getStaff(vanishedPlayer.getUuid());
            if (vanishing == null) return true; // no longer staff -> fully visible
            StaffMember target = StaffManager.getInstance().getStaff(viewer.getUuid());
            if (target == null) return false; // regular players cannot see
            return vanishing.getRank().ordinal() <= target.getRank().ordinal();
        });
    }

    public static void register() {
        // Load staff from the API into the in-memory cache on startup.
        StaffManager.getInstance().loadStaff();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(PlayerLoadedEvent.class, event -> {
            Player player = event.getPlayer();
            UUID playerUuid = player.getUuid();

            if (StaffManager.getInstance().isStaff(playerUuid)) {
                StaffMember staff = StaffManager.getInstance().getStaff(playerUuid);
                player.setPermissionLevel(staff.getRank().getPermissionLevel());
                player.sendMessage(Messages.info("[StaffSystem] Loaded in as " + staff.getRank()));
            }
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player joiningPlayer = event.getPlayer();

            // If the joining player is themselves vanished, re-apply the hide rule
            // and let them know. The rule is per-entity, so it must be set again on join.
            StaffMember joiningStaff = StaffManager.getInstance().getStaff(joiningPlayer.getUuid());
            if (joiningStaff != null && joiningStaff.isVanished()) {
                applyVanishRule(joiningPlayer);
                joiningPlayer.sendMessage(Messages.system("You have joined in vanish!"));
                joiningPlayer.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Potion.INFINITE_DURATION));
            }
        });
    }
}
