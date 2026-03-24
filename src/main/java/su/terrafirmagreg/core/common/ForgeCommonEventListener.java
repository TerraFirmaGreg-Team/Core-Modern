package su.terrafirmagreg.core.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;

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
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggHandler;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;
import su.terrafirmagreg.core.common.perf.SupportCache;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.FuelSyncPacket;
import su.terrafirmagreg.core.utils.commands.TFGCommands;
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
