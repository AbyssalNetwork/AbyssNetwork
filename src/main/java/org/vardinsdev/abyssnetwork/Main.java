package org.vardinsdev.abyssnetwork;

import net.hollowcube.polar.PolarLoader;
import net.minestom.server.timer.SchedulerManager;
import org.vardinsdev.abyssnetwork.commands.GiveCommand;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.vardinsdev.abyssnetwork.commands.GiveRankCommand;
import org.vardinsdev.abyssnetwork.commands.HelpCommand;
import org.vardinsdev.abyssnetwork.commands.VanishCommand;
import org.vardinsdev.abyssnetwork.events.ChatHandler;
import org.vardinsdev.abyssnetwork.events.GamemodeSwitcher;
import org.vardinsdev.abyssnetwork.events.PlayerConfiguration;
import org.vardinsdev.abyssnetwork.events.PlayerLoaded;
import org.vardinsdev.abyssnetwork.staff.StaffSystemExtension;
import org.vardinsdev.minegun.HealthManagement;
import org.vardinsdev.minegun.Weapons.Rifle;
import org.vardinsdev.minegun.Weapons.RocketLauncher;
import rocks.minestom.placement.BannerPlacementRule;
import rocks.minestom.placement.Utility;
import rocks.minestom.placement.*;

import java.io.IOException;
import java.nio.file.Path;

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
    public static void main(String[] args) {
        AbyssLogger.printBanner();
        AbyssLogger.info("Starting Abyss Network...");

        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());
        AbyssLogger.success("Server Initiated!");
        registerPlacementRules();
        AbyssLogger.success("Placement Rules Registered!");

        // Instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        StaffSystemExtension.register();
        MinecraftServer.getCommandManager().register(new GiveRankCommand());
        MinecraftServer.getCommandManager().register(new VanishCommand());

        PlayerConfiguration.register(instanceContainer);
        PlayerLoaded.register();

        HealthManagement.register();
        Rifle.register(instanceContainer);
        RocketLauncher.register(instanceContainer);

        ChatHandler.register();

        MinecraftServer.getCommandManager().register(new GiveCommand());
        MinecraftServer.getCommandManager().register(new HelpCommand());

        GamemodeSwitcher.register();

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
}


