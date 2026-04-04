package org.vardinsdev.abyssnetwork;

import io.github.cdimascio.dotenv.Dotenv;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.SchedulerManager;
import org.vardinsdev.abyssnetwork.Database.DatabaseManager;
import org.vardinsdev.minegun.Events.PlayerLoadedEventHandler;
import org.vardinsdev.minegun.Events.PlayerTickEventHandler;
import org.vardinsdev.minegun.Weapons.Rifle;
import org.vardinsdev.minegun.Weapons.RocketLauncher;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.vardinsdev.minegun.demo.TestDummy;
import org.vardinsdev.minegun.demo.giveCommand;
import org.vardinsdev.minegun.minegunLogger;
import rocks.minestom.placement.BannerPlacementRule;
import rocks.minestom.placement.Utility;
import rocks.minestom.placement.*;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class Main {
    private static void registerPlacementRules() {
        Utility.registerPlacementRules(
                AxisPlacementRule::new,
                Block.CREAKING_HEART,
                Block.HAY_BLOCK,
                Block.IRON_CHAIN,
                Block.DEEPSLATE,
                Block.INFESTED_DEEPSLATE,
                Block.MUDDY_MANGROVE_ROOTS,
                Block.BAMBOO_BLOCK,
                Block.STRIPPED_BAMBOO_BLOCK,
                Block.BASALT,
                Block.POLISHED_BASALT,
                Block.QUARTZ_PILLAR,
                Block.PURPUR_PILLAR,
                Block.BONE_BLOCK,
                Block.OCHRE_FROGLIGHT,
                Block.VERDANT_FROGLIGHT,
                Block.PEARLESCENT_FROGLIGHT);

        Utility.registerPlacementRules(StairPlacementRule::new, StairPlacementRule.KEY);
        Utility.registerPlacementRules(SlabPlacementRule::new, SlabPlacementRule.KEY);
        Utility.registerPlacementRules(FencePlacementRule::new, FencePlacementRule.KEY);
        Utility.registerPlacementRules(FenceGatePlacementRule::new, FenceGatePlacementRule.KEY);
        Utility.registerPlacementRules(WallPlacementRule::new, WallPlacementRule.KEY);
        Utility.registerPlacementRules(GlassPanePlacementRule::new,
                Block.GLASS_PANE,
                Block.IRON_BARS,
                Block.WHITE_STAINED_GLASS_PANE,
                Block.ORANGE_STAINED_GLASS_PANE,
                Block.MAGENTA_STAINED_GLASS_PANE,
                Block.LIGHT_BLUE_STAINED_GLASS_PANE,
                Block.YELLOW_STAINED_GLASS_PANE,
                Block.LIME_STAINED_GLASS_PANE,
                Block.PINK_STAINED_GLASS_PANE,
                Block.GRAY_STAINED_GLASS_PANE,
                Block.LIGHT_GRAY_STAINED_GLASS_PANE,
                Block.CYAN_STAINED_GLASS_PANE,
                Block.PURPLE_STAINED_GLASS_PANE,
                Block.BLUE_STAINED_GLASS_PANE,
                Block.BROWN_STAINED_GLASS_PANE,
                Block.GREEN_STAINED_GLASS_PANE,
                Block.RED_STAINED_GLASS_PANE,
                Block.BLACK_STAINED_GLASS_PANE);

        Utility.registerPlacementRules(DoorPlacementRule::new, DoorPlacementRule.KEY);
        Utility.registerPlacementRules(BedPlacementRule::new, BedPlacementRule.KEY);
        Utility.registerPlacementRules(ButtonPlacementRule::new, ButtonPlacementRule.KEY);
        Utility.registerPlacementRules(TrapdoorPlacementRule::new, TrapdoorPlacementRule.KEY);
        Utility.registerPlacementRules(StandingSignPlacementRule::new, StandingSignPlacementRule.KEY);
        Utility.registerPlacementRules(WallSignPlacementRule::new, WallSignPlacementRule.KEY);
        Utility.registerPlacementRules(CeilingHangingSignPlacementRule::new, CeilingHangingSignPlacementRule.KEY);
        Utility.registerPlacementRules(WallHangingSignPlacementRule::new, WallHangingSignPlacementRule.KEY);
        Utility.registerPlacementRules(BannerPlacementRule::new, BannerPlacementRule.KEY);
        Utility.registerPlacementRules(HorizontalFacingPlacementRule::new, Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER, Block.STONECUTTER);
        Utility.registerPlacementRules(ChestPlacementRule::new, Block.CHEST);
        Utility.registerPlacementRules(PlantPlacementRule::new, PlantPlacementRule.KEY);
        Utility.registerPlacementRules(CropPlacementRule::new, CropPlacementRule.KEY);
        Utility.registerPlacementRules(TallPlantPlacementRule::new,
                Block.SUNFLOWER,
                Block.LILAC,
                Block.PEONY,
                Block.ROSE_BUSH,
                Block.TALL_GRASS,
                Block.LARGE_FERN,
                Block.TALL_SEAGRASS,
                Block.PITCHER_PLANT);

        Utility.registerPlacementRules(MushroomPlacementRule::new, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM);
        Utility.registerPlacementRules(SugarCanePlacementRule::new, Block.SUGAR_CANE);
        Utility.registerPlacementRules(CactusPlacementRule::new, Block.CACTUS);
        Utility.registerPlacementRules(CactusFlowerPlacementRule::new, Block.CACTUS_FLOWER);
        Utility.registerPlacementRules(RailPlacementRule::new, RailPlacementRule.KEY);
    }
    static void main() {
        AbyssLogger.printBanner();
        AbyssLogger.info("Starting Abyss Network...");

        Dotenv env = Dotenv.load();

        if (!Objects.equals(env.get("TYPE"), "dev")) {
            try {
                DatabaseManager.connect(
                        env.get("DB_HOST"),
                        Integer.parseInt(env.get("DB_PORT")),
                        env.get("DB_NAME"),
                        env.get("DB_USER"),
                        env.get("DB_PASSWORD")
                );
                AbyssLogger.success("Connected to the database!");
            } catch (SQLException e) {
                AbyssLogger.error("Failed to connect to the database: " + e.getMessage());
                return; // Stop the server from starting if the DB is required
            }
        } else {
            AbyssLogger.info("You are running in dev mode! No database connections made!");
        }



        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());
        AbyssLogger.success("Server Initiated!");
        registerPlacementRules();
        AbyssLogger.success("Placement Rules Registered!");

        registerEvents();

        // Instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setChunkSupplier(LightingChunk::new);

        // World defining
        try {
            instanceContainer.setChunkLoader(new PolarLoader(Path.of("worlds/world.polar")));
        } catch (IOException o) {
            AbyssLogger.error("Failed to load world: " + o.getMessage());
        }
        /*
        Server off save
         */
        SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildShutdownTask(() -> {
            try {
                DatabaseManager.disconnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            MinecraftServer.getConnectionManager().shutdown();
            try {
                instanceContainer.saveChunksToStorage();
                AbyssLogger.info("World Saved!");
                Thread.sleep(500);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            AbyssLogger.warn("The server is shutting down!");
        });

        minecraftServer.start("0.0.0.0", 25565);
        AbyssLogger.success("Server started on port 25565");
    }
    public static void registerEvents() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            String uuid = player.getUuid().toString();
            String name = player.getUsername();

            try (Connection conn = DatabaseManager.getConnection()) {
                // Using "ON DUPLICATE KEY UPDATE" handles both new and returning players in one go
                String query = "INSERT INTO players (uuid, username) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = ?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, uuid);
                    stmt.setString(2, name);
                    stmt.setString(3, name);
                    stmt.executeUpdate();
                }

                AbyssLogger.info("Player data synchronized for: " + name);
            } catch (SQLException e) {
                AbyssLogger.error("Could not sync player data for " + name + ": " + e.getMessage());
            }
        });
    }
}


