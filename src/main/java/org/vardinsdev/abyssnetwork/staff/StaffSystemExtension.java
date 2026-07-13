package org.vardinsdev.abyssnetwork.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.UUID;

public class StaffSystemExtension {

    public static void register() {
        // 1. Initial load from JSON into memory cache when server registers this system
        StaffManager.getInstance().loadStaff();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            UUID playerUuid = player.getUuid();

            // 2. Simply check the cache! No slow file reading on join thread anymore
            if (StaffManager.getInstance().isStaff(playerUuid)) {
                StaffMember staff = StaffManager.getInstance().getStaff(playerUuid);
                System.out.println("[StaffSystem] Loaded " + player.getUsername() + " as " + staff.getRank());
            }
        });

        globalEventHandler.addListener(PlayerLoadedEvent.class, event -> {
            Player player = event.getPlayer();
            UUID playerUuid = player.getUuid();

            if (StaffManager.getInstance().isStaff(playerUuid)) {
                StaffMember staff = StaffManager.getInstance().getStaff(playerUuid);
                player.sendMessage(Component.text("[StaffSystem] Loaded in as " + staff.getRank()).color(NamedTextColor.RED));
            }
        });
        // Add this inside your existing register() method in StaffSystemExtension
        globalEventHandler.addListener(net.minestom.server.event.player.PlayerSpawnEvent.class, event -> {
            Player joiningPlayer = event.getPlayer();
            boolean isJoiningStaff = StaffManager.getInstance().isStaff(joiningPlayer.getUuid());
            StaffMember staff = StaffManager.getInstance().getStaff(joiningPlayer.getUuid());
            if (staff != null && staff.isVanished()) {
                joiningPlayer.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                joiningPlayer.sendMessage(Component.text("You have joined in vanish!").color(NamedTextColor.AQUA));
                joiningPlayer.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Potion.INFINITE_DURATION));
            }

            // If the joining player is a regular player, hide all currently vanished staff from them
            if (!isJoiningStaff) {
                for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    if (StaffManager.getInstance().isStaff(onlinePlayer.getUuid())) {
                        staff = StaffManager.getInstance().getStaff(onlinePlayer.getUuid());
                        if (staff.isVanished()) {
                            // Hide the vanished staff member from this new regular player
                            onlinePlayer.removeViewer(joiningPlayer);
                        }
                    }
                }
            }
        });
    }
}