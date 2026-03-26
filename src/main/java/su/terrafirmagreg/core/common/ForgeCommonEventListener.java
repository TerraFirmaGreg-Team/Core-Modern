package su.terrafirmagreg.core.common;

import java.util.Objects;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.MissingMappingsEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.capability.LargeEggCapability;
import su.terrafirmagreg.core.common.capability.LargeEggHandler;
import su.terrafirmagreg.core.common.data.TFGCommands;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.perf.SupportCache;
import su.terrafirmagreg.core.common.data.tfgt.TFGMultiMachines;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.FuelSyncPacket;
import su.terrafirmagreg.core.utils.CustomSpawnHelper;
import su.terrafirmagreg.core.utils.CustomSpawnSaveHandler;
import su.terrafirmagreg.core.world.BedrockFluidFeatureGenerator;
import su.terrafirmagreg.core.world.BedrockFluidSpoutLoader;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class ForgeCommonEventListener {

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
            //Send the blaze burner liquid fuel map to send to the client and populate emi.
            TFGNetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new FuelSyncPacket(FuelSyncPacket.capturedJsonData));

            //Checks if the player is in a custom dimension spawn,
            // and puts them at that pos when they first join
            GlobalPos spawnPos = CustomSpawnSaveHandler.getSpawnPos(Objects.requireNonNull(player.getServer()).overworld());

            if (!spawnPos.dimension().equals(ServerLevel.OVERWORLD)) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag tfgPlayerData;

                if (playerData.contains(TFGCore.MOD_ID, CompoundTag.TAG_COMPOUND)) {
                    tfgPlayerData = playerData.getCompound(TFGCore.MOD_ID);
                } else {
                    tfgPlayerData = new CompoundTag();
                    playerData.put(TFGCore.MOD_ID, tfgPlayerData);
                }

                if (!tfgPlayerData.getBoolean("hasJoinedBefore")) {
                    tfgPlayerData.putBoolean("hasJoinedBefore", true);
                    playerData.put(TFGCore.MOD_ID, tfgPlayerData);

                    CustomSpawnHelper.respawnTeleporter(player, player.getServer().getLevel(spawnPos.dimension()), spawnPos);

                }

            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            GlobalPos worldSpawn = CustomSpawnSaveHandler.getSpawnPos(Objects.requireNonNull(server).overworld());

            if ((worldSpawn.dimension().equals(ServerLevel.OVERWORLD) || player.getRespawnPosition() != null))
                return;
            CustomSpawnHelper.respawnTeleporter(player, server.getLevel(worldSpawn.dimension()), worldSpawn);
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

        String veinId = entry.getVeinId();

        ResourceLocation featureId = BedrockFluidSpoutLoader.VEIN_TO_FEATURE.get(veinId);
        if (featureId == null)
            return;

        String type = BedrockFluidSpoutLoader.VEIN_TO_TYPE.get(veinId);
        if (type == null)
            return;

        switch (type) {
            case "spout" -> BedrockFluidFeatureGenerator.generateSpout(serverLevel, chunkPos, featureId);
            case "structure" -> BedrockFluidFeatureGenerator.generateStructure(serverLevel, chunkPos, featureId);
            case "pool" -> BedrockFluidFeatureGenerator.generatePool(serverLevel, chunkPos, featureId);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ClientLevel)
            return;

        MinecraftServer server = Objects.requireNonNull(level.getServer());
        if (server.overworld() != level) {
            GlobalPos spawnPos = CustomSpawnSaveHandler.getSpawnPos(server.overworld());

            var targetLevel = server.getLevel(spawnPos.dimension());
            if (targetLevel == level) {

                RandomSource random = new XoroshiroRandomSource(targetLevel.getSeed());

                BlockPos validSpawn = null;

                int chunkSearchRadius = 32;

                int count = 0;
                while (Objects.isNull(validSpawn)) {
                    ChunkPos chunkPos = new ChunkPos(random.nextInt(chunkSearchRadius * 2) - chunkSearchRadius, random.nextInt(chunkSearchRadius * 2) - chunkSearchRadius);
                    //System.out.println("ChunkPos: " + chunkPos);
                    var testPos = chunkPos.getMiddleBlockPosition(128);

                    int buildHeightLimit = Math.min(targetLevel.getMaxBuildHeight(), targetLevel.getMinBuildHeight() + targetLevel.getLogicalHeight()) - 1;
                    BlockPos.MutableBlockPos mutableTestPos = testPos.mutable();

                    //System.out.println(targetLevel.getBlockState(mutableTestPos).getBlock().toString() + targetLevel.getBlockState(mutableTestPos.above()).getBlock());

                    ChunkAccess chunk = targetLevel.getChunk(mutableTestPos.immutable());

                    for (int testY = buildHeightLimit; testY > 0; testY--) {
                        mutableTestPos.setY(testY);

                        /*System.out.println("\tBlocks");
                        System.out.println("spawn point: " + mutableTestPos);*/
                        var blockA = chunk.getBlockState(mutableTestPos.above());
                        var blockB = chunk.getBlockState(mutableTestPos);
                        var blockC = chunk.getBlockState(mutableTestPos.below());

                        /*System.out.println(blockA);
                        System.out.print(blockA.isAir());
                        System.out.println(blockB);
                        System.out.print(!blockB.isCollisionShapeFullBlock(targetLevel, mutableTestPos.immutable()));
                        System.out.println(blockC);
                        System.out.print(blockC.isCollisionShapeFullBlock(targetLevel, mutableTestPos.immutable()));
                        
                         */

                        if (blockA.isAir() && !blockB.isCollisionShapeFullBlock(targetLevel, mutableTestPos.immutable())) {
                            if (blockC.isCollisionShapeFullBlock(targetLevel, mutableTestPos.immutable())) {
                                ResourceKey<Biome> biomeKey = targetLevel.getBiome(mutableTestPos).unwrapKey().orElse(null);

                                //System.out.println(biomeKey);
                                if (Objects.nonNull(biomeKey) && biomeKey.location().equals(ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "nether/lush_hollow"))) {
                                    validSpawn = mutableTestPos.immutable();
                                }
                                //If it is not in the right biome it is unlikely that going down more will be in the valid biome
                                break;
                            }
                        }

                    }

                    if (count >= 25) {
                        //validSpawn = BlockPos.ZERO;
                        chunkSearchRadius = chunkSearchRadius * 2;
                        count = -1;
                        //System.out.println("No Valid Spawn :(, trying search radius of " + chunkSearchRadius);
                    }

                    count++;
                }

                TFGCore.LOGGER.info("Found valid spawn point: {}", validSpawn);
                CustomSpawnSaveHandler.setSpawnPos(server.overworld(), GlobalPos.of(spawnPos.dimension(), validSpawn));
            }
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
