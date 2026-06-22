package org.vardinsdev.abyssnetwork.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import org.vardinsdev.abyssnetwork.AbyssLogger;

public class ChatHandlerEvent {
    public static void register() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerChatEvent.class, event -> {
            if (event.getRawMessage().startsWith("%")) {
                if (event.getPlayer().getPermissionLevel() >= 2) {
                    try {
                        String formattedMessage = event.getRawMessage().replaceFirst("%", "");
                        AbyssLogger.info("Staff Chat: " + formattedMessage);
                        event.setCancelled(true);
                        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                            if (onlinePlayer.getPermissionLevel() >= 2) {
                                onlinePlayer.sendMessage(Component.text("STAFF CHAT | " + event.getPlayer().getUsername() + " " + formattedMessage).color(NamedTextColor.LIGHT_PURPLE));
                            }
                        }
                    } catch (RuntimeException e) {
                        AbyssLogger.error("Formatted Message did not get formatted properly!" + e);
                    }
                }
            } else {
                AbyssLogger.info("Chat: " + event.getRawMessage());
                event.setCancelled(true);
                for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    onlinePlayer.sendMessage(Component.text(event.getPlayer().getUsername() + " >> " + event.getRawMessage()));
                }
            }
        });
    }
}
