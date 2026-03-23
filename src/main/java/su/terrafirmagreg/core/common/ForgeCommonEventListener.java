package su.terrafirmagreg.core.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggHandler;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;
import su.terrafirmagreg.core.common.perf.SupportCache;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.FuelSyncPacket;
import su.terrafirmagreg.core.utils.commands.TFGCommands;
import su.terrafirmagreg.core.world.BedrockFluidSpoutLoader;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class ForgeCommonEventListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Block cachedDryIce = null;
    private static Block cachedGeyserite = null;

    private static boolean loadGasVentBlocks() {
        if (cachedDryIce != null && cachedGeyserite != null)
            return true;
        cachedDryIce = TFGBlocks.DRY_ICE.get();
        cachedGeyserite = ForgeRegistries.BLOCKS
                .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "rock/raw/geyserite"));
        return cachedDryIce != null && cachedGeyserite != null;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        TFGCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!stack.isEmpty()) {
            if (stack.getItem() == TFGItems.SNIFFER_EGG.get() || stack.getItem() == TFGItems.WRAPTOR_EGG.get()) {
                event.addCapability(LargeEggCapability.KEY, new LargeEggHandler(stack));
            }
        }
    }

    /**
     * Send the blaze burner liquid fuel map to send to the client and populate emi.
     */

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TFGNetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new FuelSyncPacket(FuelSyncPacket.capturedJsonData));
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            SupportCache.clearLevel(level);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(BedrockFluidSpoutLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide())
            return;
        if (!event.isNewChunk())
            return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel))
            return;

        ChunkPos chunkPos = event.getChunk().getPos();

        var savedData = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
        var entry = savedData.getFluidVeinWorldEntry(chunkPos.x, chunkPos.z);

        if (entry == null || entry.getVeinId() == null)
            return;

        ResourceLocation featureId = BedrockFluidSpoutLoader.VEIN_TO_FEATURE.get(entry.getVeinId());
        if (featureId == null)
            return;

        String type = BedrockFluidSpoutLoader.VEIN_TO_TYPE.getOrDefault(entry.getVeinId(), "spout");

        if ("spout".equals(type)) {
            var cfRegistry = serverLevel.registryAccess()
                    .registry(Registries.CONFIGURED_FEATURE)
                    .orElse(null);
            if (cfRegistry == null)
                return;

            var configuredFeature = cfRegistry.get(featureId);
            if (configuredFeature == null)
                return;

            if (!(configuredFeature.feature() instanceof com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature))
                return;

            var config = (com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration) configuredFeature.config();

            serverLevel.getServer().execute(() -> {
                int x = chunkPos.getMiddleBlockX();
                int z = chunkPos.getMiddleBlockZ();
                int surfaceY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

                if (serverLevel.random.nextFloat() > config.sproutChance())
                    return;

                var fluid = config.fluid();
                if (fluid == null || fluid.isSame(net.minecraft.world.level.material.Fluids.EMPTY))
                    return;

                var blockState = fluid.defaultFluidState().createLegacyBlock();

                int size = config.size().sample(serverLevel.random);
                int surfaceOffset = config.surfaceOffset().sample(serverLevel.random);

                int minY = serverLevel.getMinBuildHeight() + size + 5;
                int maxOriginY = Math.max(minY + 1, surfaceY / 2);
                int originY = minY + serverLevel.random.nextInt(Math.max(1, maxOriginY - minY));

                int radius = net.minecraft.util.Mth.ceil(size / 2f);
                int x0 = x - radius;
                int y0 = originY - radius;
                int z0 = z - radius;
                int width = size + 1;
                int height = size + 1;
                int length = size + 1;

                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                for (int dx = 0; dx < width; dx++) {
                    float fx = dx * 2f / width - 1;
                    if (fx * fx > 1)
                        continue;

                    for (int dy = 0; dy < height; dy++) {
                        float fy = dy * 2f / height - 1;
                        if (fx * fx + fy * fy > 1)
                            continue;

                        int currentY = y0 + dy;
                        if (serverLevel.isOutsideBuildHeight(currentY))
                            continue;

                        for (int dz = 0; dz < length; dz++) {
                            float fz = dz * 2f / length - 1;
                            if (fx * fx + fy * fy + fz * fz > 1)
                                continue;

                            mutablePos.set(x0 + dx, currentY, z0 + dz);
                            if (!serverLevel.isOutsideBuildHeight(mutablePos))
                                serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);
                        }
                    }
                }

                int topY = surfaceY + surfaceOffset;
                for (int currentY = originY; currentY <= topY; currentY++) {
                    mutablePos.set(x, currentY, z);
                    if (serverLevel.isOutsideBuildHeight(mutablePos))
                        continue;
                    serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);

                    if (currentY <= surfaceY) {
                        mutablePos.set(x + 1, currentY, z);
                        if (!serverLevel.isOutsideBuildHeight(mutablePos))
                            serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);
                        mutablePos.set(x - 1, currentY, z);
                        if (!serverLevel.isOutsideBuildHeight(mutablePos))
                            serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);
                        mutablePos.set(x, currentY, z + 1);
                        if (!serverLevel.isOutsideBuildHeight(mutablePos))
                            serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);
                        mutablePos.set(x, currentY, z - 1);
                        if (!serverLevel.isOutsideBuildHeight(mutablePos))
                            serverLevel.getChunk(mutablePos).setBlockState(mutablePos, blockState, false);
                    }
                }

                //LOGGER.debug("[FluidVeins] Spout placed at {},{},{} for vein {}", x, topY, z, entry.getVeinId());
            });

        } else if ("structure".equals(type)) {
            serverLevel.getServer().execute(() -> {
                if (serverLevel.random.nextFloat() > 0.05f) // Spawn rate
                    return;

                int x = chunkPos.getMiddleBlockX();
                int z = chunkPos.getMiddleBlockZ();
                int surfaceY = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

                if (!loadGasVentBlocks()) {
                    LOGGER.warn("[FluidVeins] Do blocks exists");
                    return;
                }

                var dryIce = cachedDryIce.defaultBlockState();
                var geyserite = cachedGeyserite.defaultBlockState();

                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                int baseRadius = 4;
                for (int dx = -baseRadius - 1; dx <= baseRadius + 1; dx++) {
                    for (int dz = -baseRadius - 1; dz <= baseRadius + 1; dz++) {
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        double effectiveRadius = baseRadius + (serverLevel.random.nextDouble() - 0.5) * 1.5;

                        if (dist > effectiveRadius)
                            continue;

                        mutablePos.set(x + dx, surfaceY - 1, z + dz);

                        if (dist < 1.0) {
                            serverLevel.getChunk(mutablePos).setBlockState(mutablePos, dryIce, false);
                        } else {
                            if (serverLevel.random.nextFloat() < 0.7f)
                                serverLevel.getChunk(mutablePos).setBlockState(mutablePos, geyserite, false);
                        }
                    }
                }

                //LOGGER.debug("[FluidVeins] Structure placed at {},{},{} for vein {}", x, surfaceY, z, entry.getVeinId());
            });

        }
    }

    @SubscribeEvent
    public static void remapIds(MissingMappingsEvent event) {
        event.getAllMappings(Registries.BLOCK).forEach(ForgeCommonEventListener::remapBlocks);
        event.getAllMappings(Registries.ITEM).forEach(ForgeCommonEventListener::remapItems);
        event.getAllMappings(Registries.BLOCK_ENTITY_TYPE).forEach(ForgeCommonEventListener::remapBlockEntities);
    }

    private static void remapBlocks(MissingMappingsEvent.Mapping<Block> mapping) {
        if (mapping.getKey() == GTCEu.id("heat_exchanger"))
            mapping.remap(TFGMultiMachines.HEAT_EXCHANGER.getBlock());
    }

    private static void remapItems(MissingMappingsEvent.Mapping<Item> mapping) {
        if (mapping.getKey() == GTCEu.id("heat_exchanger"))
            mapping.remap(TFGMultiMachines.HEAT_EXCHANGER.getItem());
    }

    private static void remapBlockEntities(MissingMappingsEvent.Mapping<BlockEntityType<?>> mapping) {
        if (mapping.getKey() == GTCEu.id("heat_exchanger"))
            mapping.remap(TFGMultiMachines.HEAT_EXCHANGER.getBlockEntityType());
    }
}
