package org.vardinsdev.abyssnetwork.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;
import org.vardinsdev.abyssnetwork.staff.StaffSystemExtension;

public class VanishCommand extends Command {
    public VanishCommand() {
        super("vanish");

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                if (StaffManager.getInstance().isStaff(player.getUuid())) {
                    StaffMember staff = StaffManager.getInstance().getStaff(player.getUuid());

                    boolean nextVanishState = !staff.isVanished();
                    staff.setVanished(nextVanishState);

                    if (nextVanishState) {
                        // Hide from regular players and lower-ranked staff. Minestom re-applies
                        // this rule automatically (new joins, chunk loads), so the vanish sticks.
                        StaffSystemExtension.applyVanishRule(player);

                        // Notify equal-or-higher ranked staff
                        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                            if (onlinePlayer == player) continue;
                            StaffMember target = StaffManager.getInstance().getStaff(onlinePlayer.getUuid());
                            if (target != null && staff.getRank().ordinal() <= target.getRank().ordinal()) {
                                onlinePlayer.sendMessage(Component.text("[STAFF] " + player.getUsername() + " has vanished").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                            }
                        }

                        player.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                        player.sendMessage(Component.text("You are now vanished! (Hidden from regular players)").color(NamedTextColor.AQUA));
                        player.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Potion.INFINITE_DURATION));

                    } else {
                        player.updateViewableRule(null);

                        player.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                        player.sendMessage(Component.text("You are no longer vanished.").color(NamedTextColor.AQUA));
                        player.removeEffect(PotionEffect.NIGHT_VISION);
                    }

                    StaffManager.getInstance().updateStaff(staff);
                } else {
                    sender.sendMessage(Component.text("Abyss Network System").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                    sender.sendMessage(Component.text("You must be a staff member to use this command!").color(NamedTextColor.AQUA));
                }
            } else {
                AbyssLogger.error("Console tried to vanish itself!");
            }
        });
    }
}