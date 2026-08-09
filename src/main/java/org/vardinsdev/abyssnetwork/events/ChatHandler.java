package org.vardinsdev.abyssnetwork.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;

public class ChatHandler {
    public static void register() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();
            String rawMessage = event.getRawMessage();

            // Check if the message is intended for staff chat
            if (rawMessage.startsWith("%")) {
                // 1. Check if the sender is a staff member using the cache
                if (StaffManager.getInstance().isStaff(player.getUuid())) {
                    try {
                        String formattedMessage = rawMessage.replaceFirst("%", "").trim();
                        AbyssLogger.info("Staff Chat: [" + player.getUsername() + "] " + formattedMessage);
                        event.setCancelled(true);

                        // 2. Distribute only to online staff tracked by StaffManager
                        StaffMember staff = StaffManager.getInstance().getStaff(player.getUuid());
                        String rankPrefix = staff != null ? "[" + staff.getRank().getDisplayRank() + "] " : "";
                        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                            if (StaffManager.getInstance().isStaff(onlinePlayer.getUuid())) {
                                onlinePlayer.sendMessage(Component.text()
                                        .append(Component.text("STAFF CHAT | ", NamedTextColor.LIGHT_PURPLE))
                                        .append(Component.text(rankPrefix + player.getUsername() + ": ", NamedTextColor.AQUA))
                                        .append(Component.text(formattedMessage, NamedTextColor.LIGHT_PURPLE))
                                        .build());
                            }
                        }
                    } catch (Exception e) {
                        AbyssLogger.error("Staff chat failed to process properly: " + e.getMessage());
                    }
                } else {
                    // Optional fallback: If a normal player types %, treat it as regular chat
                    sendGlobalChat(player, rawMessage);
                    event.setCancelled(true);
                }
            } else {
                // Handle regular global chat
                sendGlobalChat(player, rawMessage);
                event.setCancelled(true);
            }
        });
    }

    private static void sendGlobalChat(Player player, String message) {
        StaffMember staff = StaffManager.getInstance().getStaff(player.getUuid());

        AbyssLogger.info("Chat: " + player.getUsername() + " >> " + message);

        Component chatComponent;
        if (staff != null) {
            // Prefix staff chat with their rank, coloured with the rank colour
            chatComponent = Component.text()
                    .append(Component.text("[", NamedTextColor.GRAY))
                    .append(Component.text(staff.getRank().getDisplayRank(), staff.getRank().getColor()))
                    .append(Component.text("] ", NamedTextColor.GRAY))
                    .append(Component.text(player.getUsername() + " >> ", NamedTextColor.AQUA))
                    .append(Component.text(message, NamedTextColor.WHITE))
                    .build();
        } else {
            chatComponent = Component.text(player.getUsername() + " >> " + message);
        }

        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(chatComponent);
        }
    }
}