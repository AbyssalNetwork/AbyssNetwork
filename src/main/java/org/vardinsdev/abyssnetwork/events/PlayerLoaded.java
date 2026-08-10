package org.vardinsdev.abyssnetwork.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoadedEvent;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Database.ApiClient;
import org.vardinsdev.abyssnetwork.Messages;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;

public class PlayerLoaded {
    public static void register() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(PlayerLoadedEvent.class, event -> {
            Player joining = event.getPlayer();
            AbyssLogger.success(joining.getUsername() + " has joined the server!");

            // Enforce bans before announcing the join. The check is async, so the
            // join broadcast is deferred until we know the player isn't banned.
            ApiClient.getInstance().fetchBan(joining.getUuid().toString()).thenAccept(ban -> {
                if (ban != null) {
                    MinecraftServer.getSchedulerManager().scheduleNextTick(() ->
                            joining.kick(Messages.system("You are banned from Abyss Network.", ban.getReason())));
                    return;
                }
                broadcastJoin(joining);
            });
        });

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player leaving = event.getPlayer();
            AbyssLogger.info(leaving.getUsername() + " has left the server!");
            broadcastLeave(leaving);
        });
    }

    private static void broadcastJoin(Player joining) {
        // A vanished staff member's join is only announced to people who can
        // actually see them (equal-or-higher ranked staff).
        StaffMember joiningStaff = StaffManager.getInstance().getStaff(joining.getUuid());
        boolean joiningVanished = joiningStaff != null && joiningStaff.isVanished();

        var message = Messages.announce(joining.getUsername() + " has joined the server!");
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer == joining) continue;
            if (joiningVanished && !canSee(joiningStaff, onlinePlayer)) continue;
            onlinePlayer.sendMessage(message);
        }
    }

    private static void broadcastLeave(Player leaving) {
        StaffMember leavingStaff = StaffManager.getInstance().getStaff(leaving.getUuid());
        boolean leavingVanished = leavingStaff != null && leavingStaff.isVanished();

        var message = Messages.announce(leaving.getUsername() + " has left the server!");
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer == leaving) continue;
            if (leavingVanished && !canSee(leavingStaff, onlinePlayer)) continue;
            onlinePlayer.sendMessage(message);
        }
    }

    private static boolean canSee(StaffMember vanished, Player viewer) {
        StaffMember viewerStaff = StaffManager.getInstance().getStaff(viewer.getUuid());
        if (viewerStaff == null) return false; // regular players cannot see vanished staff
        return vanished.getRank().ordinal() <= viewerStaff.getRank().ordinal();
    }
}
