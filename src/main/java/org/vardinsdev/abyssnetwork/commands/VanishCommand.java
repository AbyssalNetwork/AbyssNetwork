package org.vardinsdev.abyssnetwork.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.vardinsdev.abyssnetwork.staff.StaffManager;
import org.vardinsdev.abyssnetwork.staff.StaffMember;
import org.vardinsdev.minegun.minegunLogger;

public class VanishCommand extends Command {
    public VanishCommand() {
        super("vanish");

        addSyntax((sender, context) -> {
            if (sender instanceof Player p) {
                if (StaffManager.getInstance().isStaff(p.getUuid())) {
                    StaffMember staff = StaffManager.getInstance().getStaff(p.getUuid());

                    boolean nextVanishState = !staff.isVanished();
                    staff.setVanished(nextVanishState);

                    // Loop through all online players to update who can see this staff member
                    for (Player onlinePlayer : net.minestom.server.MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        if (onlinePlayer.equals(p)) continue; // Skip self

                        boolean isTargetStaff = StaffManager.getInstance().isStaff(onlinePlayer.getUuid());

                        if (nextVanishState) {
                            // We are vanishing! Hide from normal players, keep visible for staff
                            if (!isTargetStaff) {
                                p.removeViewer(onlinePlayer);
                            }
                        } else {
                            // We are unvanishing! Show to everyone
                            p.addViewer(onlinePlayer);
                        }
                    }

                    if (nextVanishState) {
                        p.sendMessage("You are now vanished! (Hidden from regular players)");
                        p.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Potion.INFINITE_DURATION));

                    } else {
                        p.sendMessage("You are no longer vanished.");
                        p.removeEffect(PotionEffect.NIGHT_VISION);
                    }

                    StaffManager.getInstance().saveStaffAsync();
                } else {
                    sender.sendMessage("You must be a staff member to use this command!");
                }
            } else {
                minegunLogger.error("Console tried to vanish itself!");
            }
        });
    }
}