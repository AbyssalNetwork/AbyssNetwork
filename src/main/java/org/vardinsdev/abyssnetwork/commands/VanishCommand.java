package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.vardinsdev.abyssnetwork.AbyssLogger;
import org.vardinsdev.abyssnetwork.Messages;
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
                                onlinePlayer.sendMessage(Messages.info("[STAFF] " + player.getUsername() + " has vanished"));
                            }
                        }

                        player.sendMessage(Messages.system("You are now vanished! (Hidden from regular players)"));
                        player.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Potion.INFINITE_DURATION));

                    } else {
                        player.updateViewableRule(null);

                        player.sendMessage(Messages.system("You are no longer vanished."));
                        player.removeEffect(PotionEffect.NIGHT_VISION);
                    }

                    StaffManager.getInstance().updateStaff(staff);
                } else {
                    sender.sendMessage(Messages.system("You must be a staff member to use this command!"));
                }
            } else {
                AbyssLogger.error("Console tried to vanish itself!");
            }
        });
    }
}