package su.terrafirmagreg.core.utils;

import java.util.*;
import java.util.function.Function;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;

public class CustomSpawnHelper {

    public static final GlobalPos BENEATH_PLACEHOLDER = GlobalPos.of(ServerLevel.NETHER, BlockPos.ZERO);
    public static final GlobalPos MARS_PLACEHOLDER = GlobalPos.of(Planet.MARS, BlockPos.ZERO);

    private static final Set<UUID> PENDING_FIRST_JOIN_TELEPORTS = new HashSet<>();

    public static boolean isUnresolvedPlaceholder(GlobalPos pos) {
        return pos.equals(BENEATH_PLACEHOLDER) || pos.equals(MARS_PLACEHOLDER);
    }

    public static void queueFirstJoinTeleport(UUID playerId) {
        PENDING_FIRST_JOIN_TELEPORTS.add(playerId);
    }

    public static void removeFirstJoinTeleport(UUID playerId) {
        PENDING_FIRST_JOIN_TELEPORTS.remove(playerId);
    }

    public static boolean respawnedAtPersonalSpawn(ServerPlayer player) {
        return player.getRespawnPosition() != null
                && player.getRespawnDimension().equals(player.level().dimension());
    }

    public static void processPendingFirstJoinTeleports(MinecraftServer server, GlobalPos worldSpawn) {
        if (isUnresolvedPlaceholder(worldSpawn)) {
            return;
        }

        ServerLevel targetLevel = server.getLevel(worldSpawn.dimension());
        if (targetLevel == null) {
            return;
        }

        for (UUID playerId : Set.copyOf(PENDING_FIRST_JOIN_TELEPORTS)) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            PENDING_FIRST_JOIN_TELEPORTS.remove(playerId);
            if (player != null) {
                tryFirstJoinTeleport(player, worldSpawn);
            }
        }
    }

    /**
     * Teleports a player to the world's custom spawn on their first join.
     *
     * @return {@code true} if the player was teleported
     */
    public static boolean tryFirstJoinTeleport(ServerPlayer player, GlobalPos spawnPos) {
        if (spawnPos.dimension().equals(ServerLevel.OVERWORLD)) {
            return false;
        }

        if (isUnresolvedPlaceholder(spawnPos)) {
            queueFirstJoinTeleport(player.getUUID());
            TFGCore.LOGGER.debug("Deferring first-join teleport for {} until custom spawn is resolved",
                    player.getGameProfile().getName());
            return false;
        }

        var tfgData = getTfgPlayerData(player);
        if (tfgData.getBoolean("hasJoinedBefore")) {
            return false;
        }

        if (player.level().dimension().equals(spawnPos.dimension())) {
            tfgData.putBoolean("hasJoinedBefore", true);
            return false;
        }

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        ServerLevel targetLevel = server.getLevel(spawnPos.dimension());
        if (targetLevel == null) {
            queueFirstJoinTeleport(player.getUUID());
            return false;
        }

        respawnTeleporter(player, targetLevel, spawnPos);
        tfgData.putBoolean("hasJoinedBefore", true);
        TFGCore.LOGGER.info("First-join teleport for {} to {}", player.getGameProfile().getName(), spawnPos);
        return true;
    }

    private static CompoundTag getTfgPlayerData(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(TFGCore.MOD_ID, CompoundTag.TAG_COMPOUND)) {
            playerData.put(TFGCore.MOD_ID, new CompoundTag());
        }
        return playerData.getCompound(TFGCore.MOD_ID);
    }

    public static void respawnTeleporter(ServerPlayer player, ServerLevel targetLevel, GlobalPos worldSpawn) {
        //System.out.println("attempting to spawn player at: " + worldSpawn);

        player.changeDimension(targetLevel, new ITeleporter() {

            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                BlockPos spawnPos = worldSpawn.pos();

                entity.teleportTo(destWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), Set.of(), entity.getYRot(), entity.getXRot());

                return entity;
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }

    /** When set (e.g. after TFCGenViewer preview Save), spawn uses world {@link net.dries007.tfc.world.settings.Settings} center/radius without climate retries. */
    public static final String VIEWER_SPAWN_ID = "tfcgenviewer";

    public static CustomSpawnCondition getFromConfig() {
        return CUSTOM_SPAWN_CONDITIONS.getOrDefault(TFGConfig.COMMON.NEW_WORLD_SPAWN.get(), DEFAULT_SPAWN);
    }

    public static void resetConfigValue() {
        TFGConfig.COMMON.NEW_WORLD_SPAWN.set(DEFAULT_SPAWN.id);
    }

    private static boolean isNewWorldSpawnViewerPreset() {
        return VIEWER_SPAWN_ID.equals(TFGConfig.COMMON.NEW_WORLD_SPAWN.get());
    }

    /**
     * Value for the create-world {@link net.minecraft.client.gui.components.CycleButton}: {@link #VIEWER_SPAWN} is not in
     * {@link #CREATE_WORLD_SPAWN_CYCLE_VALUES}, so when config is viewer-only we use {@link #DEFAULT_SPAWN} as internal placeholder.
     */
    public static CustomSpawnCondition createWorldSpawnCycleButtonValue() {
        return isNewWorldSpawnViewerPreset() ? DEFAULT_SPAWN : getFromConfig();
    }

    /** Text for the create-world spawn cycle button (keys under {@code tfg.gui.spawn_condition}); no client-only types. */
    public static Component createWorldSpawnCycleLabel(CustomSpawnCondition s) {
        if (isNewWorldSpawnViewerPreset() && DEFAULT_SPAWN.equals(s)) {
            return Component.translatable("tfg.gui.spawn_condition.tfcgenviewer");
        }
        return Component.translatable("tfg.gui.spawn_condition." + s.id()).append(" ").append(s.difficulty());
    }

    /** Tooltip body for that button; wrap with {@link net.minecraft.client.gui.components.Tooltip#create} on the client. */
    public static Component createWorldSpawnTooltipText(CustomSpawnCondition condition) {
        if (isNewWorldSpawnViewerPreset()) {
            return Component.translatable("tfg.gui.spawn_condition.tooltip.tfcgenviewer");
        }
        return Component.translatable("tfg.gui.spawn_condition.tooltip." + condition.id());
    }

    /// Outputs a list with
    ///
    /// 0: temperature multiplier
    /// 1: rainfall multiplier
    public static List<Float> findSettingsMultipliers(ChunkGeneratorExtension extension) {
        int temperatureScale = extension.settings().temperatureScale();
        int defaultTempScale = 20000;
        float tempMultiplier = (float) temperatureScale / defaultTempScale;

        int rainfallScale = extension.settings().rainfallScale();
        int defaultRainScale = 20000;
        float rainMultiplier = (float) rainfallScale / defaultRainScale;

        return new ArrayList<>(List.of(tempMultiplier, rainMultiplier));
    }

    public static boolean testWithinRanges(float temperature, float rainfall, CustomSpawnCondition condition) {
        float[] tempRange = condition.temperatureRange;
        float[] rainRange = condition.rainfallRange;

        //System.out.println(tempRange[0] + " <= " + temperature + " <= " + tempRange[1]);
        //System.out.println(rainRange[0] + " <= " + rainfall + " <= " + rainRange[1]);
        if (tempRange[0] <= temperature && temperature <= tempRange[1]) {
            //System.out.println("Temp Match");
            if (rainRange[0] <= rainfall && rainfall <= rainRange[1]) {
                //System.out.println("Rain Match");
                return true;
            }
        }
        return false;
    }

    //Adapted from TFC code, but with more config
    public static BlockPos findSpawnBiome(int spawnCenterX, int spawnCenterZ, int spawnRadius, RandomSource random, ChunkGeneratorExtension extension) {
        int step = Math.max(1, spawnRadius / 256);
        int centerX = QuartPos.fromBlock(spawnCenterX);
        int centerZ = QuartPos.fromBlock(spawnCenterZ);
        int maxRadius = QuartPos.fromBlock(spawnRadius);
        BlockPos found = null;
        int count = 0;

        for (int radius = maxRadius; radius <= maxRadius; radius += step) {
            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    int quartX = centerX + dz;
                    int quartZ = centerZ + dx;
                    BiomeExtension biome = ((BiomeSourceExtension) extension.self().getBiomeSource()).getBiomeExtensionNoRiver(quartX, quartZ);
                    if (biome.isSpawnable()) {
                        if (found == null || random.nextInt(count + 1) == 0) {
                            found = new BlockPos(QuartPos.toBlock(quartX), 0, QuartPos.toBlock(quartZ));
                        }

                        ++count;
                    }
                }
            }
        }

        if (found == null) {
            TerraFirmaCraft.LOGGER.warn("Unable to find spawn biome!");
            return new BlockPos(spawnCenterX, 0, spawnCenterZ);
        } else {
            return found;
        }
    }

    public static final TreeMap<String, CustomSpawnCondition> CUSTOM_SPAWN_CONDITIONS = new TreeMap<>();

    public static final HashMap<String, MutableComponent> SPAWN_DIFFICULTIES = new HashMap<>(Map.of(
            "easy", Component.translatable("tfg.gui.spawn_difficulty.easy"),
            "normal", Component.translatable("tfg.gui.spawn_difficulty.normal"),
            "hard", Component.translatable("tfg.gui.spawn_difficulty.hard"),
            "extreme", Component.translatable("tfg.gui.spawn_difficulty.extreme")));

    public static final CustomSpawnCondition DESERT_SPAWN = new CustomSpawnCondition(
            "desert",
            -10000,
            10000,
            1,
            new float[] { 20f, 30f },
            new float[] { 0f, 90f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("hard"));

    public static final CustomSpawnCondition TUNDRA_SPAWN = new CustomSpawnCondition(
            "tundra",
            3000,
            -10000,
            1,
            new float[] { -16f, -10f },
            new float[] { 250f, 400f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("hard"));

    public static final CustomSpawnCondition POLAR_SPAWN = new CustomSpawnCondition(
            "polar",
            -5000,
            -10000,
            1,
            new float[] { -20f, -16f },
            new float[] { 50f, 200f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("extreme"));

    public static final CustomSpawnCondition TEMPERATE_SPAWN = new CustomSpawnCondition(
            "temperate",
            2500,
            1500,
            1,
            new float[] { 5f, 15f },
            new float[] { 250f, 350f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("normal"));

    public static final CustomSpawnCondition TROPICAL_SPAWN = new CustomSpawnCondition(
            "tropical",
            10000,
            10000,
            1,
            new float[] { 20f, 30f },
            new float[] { 350f, 500f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("easy"));

    public static final CustomSpawnCondition BENEATH_SPAWN = new CustomSpawnCondition(
            "beneath",
            0,
            0,
            1,
            new float[] { -20f, 20f },
            new float[] { 0f, 400f },
            Level.NETHER,
            SPAWN_DIFFICULTIES.get("extreme"));

    public static final CustomSpawnCondition DEFAULT_SPAWN = new CustomSpawnCondition(
            "default",
            0,
            0,
            1,
            new float[] { -20f, 20f },
            new float[] { 0f, 400f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("normal"));

    /**
     * Applied when TFCGenViewer Save runs with Spawn Overlay ON. Climate ranges are unused — viewer spawn skips
     * {@link su.terrafirmagreg.core.mixins.common.tfc.ForgeEventHandlerMixin#onCreateWorldSpawn} climate matching.
     */
    public static final CustomSpawnCondition VIEWER_SPAWN = new CustomSpawnCondition(
            VIEWER_SPAWN_ID,
            0,
            0,
            1,
            new float[] { -20f, 20f },
            new float[] { 0f, 500f },
            Level.OVERWORLD,
            Component.empty());

    /**
     * Registers a new CustomSpawnCondition in the CUSTOM_SPAWN_CONDITIONS map.
     * @param condition The CustomSpawnCondition to register.
     */
    private static void initNewType(CustomSpawnCondition condition) {
        CUSTOM_SPAWN_CONDITIONS.put(condition.id, condition);
    }

    static {
        initNewType(DEFAULT_SPAWN);
        initNewType(VIEWER_SPAWN);
        initNewType(TEMPERATE_SPAWN);
        initNewType(TROPICAL_SPAWN);
        initNewType(TUNDRA_SPAWN);
        initNewType(POLAR_SPAWN);
        initNewType(DESERT_SPAWN);
        initNewType(BENEATH_SPAWN);
    }

    /**
     * Presets offered on the create-world spawn {@link net.minecraft.client.gui.components.CycleButton}.
     * {@link #VIEWER_SPAWN} is excluded: it is applied only when saving spawn in TFCGenViewer.
     * <p>
     * Ordered by difficulty: normal → easy → hard → extreme (see {@link #SPAWN_DIFFICULTIES} on each preset).
     * When adding a new preset: register with {@link #initNewType} in the static block and append here
     * (never add {@link #VIEWER_SPAWN}).
     */
    public static final List<CustomSpawnCondition> CREATE_WORLD_SPAWN_CYCLE_VALUES = List.of(
            DEFAULT_SPAWN,
            TEMPERATE_SPAWN,
            TROPICAL_SPAWN,
            TUNDRA_SPAWN,
            DESERT_SPAWN,
            POLAR_SPAWN,
            BENEATH_SPAWN);

    /**
     * Client-only create-world spawn {@link net.minecraft.client.gui.components.CycleButton} wiring.
     * Label/tooltip text stays on {@link CustomSpawnHelper}; this holds the widget ref for TFCGenViewer Save sync.
     */
    @OnlyIn(Dist.CLIENT)
    public static final class CreateWorldSpawnCycle {

        private static CycleButton<CustomSpawnCondition> cycleRef;

        private CreateWorldSpawnCycle() {
        }

        public static void register(CycleButton<CustomSpawnCondition> button) {
            cycleRef = button;
        }

        /** Sync widget from {@link TFGConfig}; safe to call from {@link net.minecraft.client.Minecraft#execute}. */
        public static void syncFromConfig() {
            var button = cycleRef;
            if (button != null) {
                CustomSpawnCondition condition = getFromConfig();
                button.setValue(createWorldSpawnCycleButtonValue());
                button.setTooltip(createTooltip(condition));
            }
        }

        public static Tooltip createTooltip(CustomSpawnCondition condition) {
            return Tooltip.create(createWorldSpawnTooltipText(condition));
        }
    }

    /// Holds spawn conditions for a particular custom world spawn
    /// @param id string used for mapping
    /// @param spawnCenterX int block pos estimate on the X axis
    /// @param spawnCenterZ int block pos estimate on the Z axis
    /// @param spawnRadiusMultiplier int multiplier on default radius
    /// @param temperatureRange inclusive range to check for the temperature
    /// @param rainfallRange inclusive range to check for the rainfall
    /// @param dimension level that this spawn occurs in
    /// @param difficulty translatable component that displays the difficulty
    public record CustomSpawnCondition(
            String id,
            int spawnCenterX,
            int spawnCenterZ,
            int spawnRadiusMultiplier,
            float[] temperatureRange,
            float[] rainfallRange,
            ResourceKey<Level> dimension,
            MutableComponent difficulty) {
    }
}
