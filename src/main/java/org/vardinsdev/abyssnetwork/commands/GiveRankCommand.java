package org.vardinsdev.abyssnetwork.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GiveRankCommand extends Command {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final File STAFF_FILE = new File("config/staff.json");

    public GiveRankCommand() {
        super("giverank");

        // Define arguments: <player name> <rank name>
        var playerArgument = ArgumentType.Word("targetPlayer");
        var rankArgument = ArgumentType.Word("rank");

        // Set command syntax
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArgument);
            String rankName = context.get(rankArgument).toUpperCase();

            // For a production server, you would ideally resolve the UUID via a Mojang API cache.
            // For this local example, we'll simulate fetching a target online player's UUID:
            Player targetPlayer = net.minestom.server.MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (targetPlayer == null) {
                sender.sendMessage("Player must be online to assign a rank via this simple command example!");
                return;
            }

            UUID uuid = targetPlayer.getUuid();

            try {
                ObjectNode rootNode;
                ObjectNode staffNode;

                // 1. Read existing file or create a fresh structure if missing
                if (STAFF_FILE.exists()) {
                    rootNode = (ObjectNode) MAPPER.readTree(STAFF_FILE);
                    staffNode = (ObjectNode) rootNode.get("staff");
                    if (staffNode == null) {
                        staffNode = MAPPER.createObjectNode();
                        rootNode.set("staff", staffNode);
                    }
                } else {
                    STAFF_FILE.getParentFile().mkdirs();
                    rootNode = MAPPER.createObjectNode();
                    staffNode = MAPPER.createObjectNode();
                    rootNode.set("staff", staffNode);
                }

                // 2. Create the updated staff member details
                ObjectNode memberDetails = MAPPER.createObjectNode();
                memberDetails.put("uuid", uuid.toString());
                memberDetails.put("lastKnownName", targetName);
                memberDetails.put("rank", rankName);
                memberDetails.put("vanished", false);

                // 3. Put it under the player's UUID key inside the "staff" block
                staffNode.set(uuid.toString(), memberDetails);

                // 4. Write the updated payload back to disk asynchronously
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        MAPPER.writeValue(STAFF_FILE, rootNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                sender.sendMessage("Successfully set " + targetName + "'s rank to " + rankName);
                targetPlayer.sendMessage("Your staff rank has been updated to " + rankName);

            } catch (Exception e) {
                sender.sendMessage("Error updating staff file!");
                e.printStackTrace();
            }

        }, playerArgument, rankArgument);
    }
}