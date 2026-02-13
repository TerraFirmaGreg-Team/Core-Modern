package su.terrafirmagreg.core.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggHandler;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!stack.isEmpty()) {
            if (stack.getItem() == TFGItems.SNIFFER_EGG.get() || stack.getItem() == TFGItems.WRAPTOR_EGG.get()) {
                event.addCapability(LargeEggCapability.KEY, new LargeEggHandler(stack));
            }
        }
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {

        event.getAllMappings(Registries.BLOCK).forEach(ForgeCommonEventListener::remapBlock);
        event.getAllMappings(Registries.ITEM).forEach(ForgeCommonEventListener::remapItem);
        event.getAllMappings(Registries.BLOCK_ENTITY_TYPE).forEach(ForgeCommonEventListener::remapBlockEntity);
    }

    public static void remapItem(MissingMappingsEvent.Mapping<Item> mapping) {
        var key = mapping.getKey();

    }

    public static void remapBlock(MissingMappingsEvent.Mapping<Block> mapping) {
        var key = mapping.getKey();
        if (key.getNamespace().equals("tfg") && key.getPath().startsWith("casings/")) {
            mapping.remap(ForgeRegistries.BLOCKS.getValue(TFGCore.id(key.getPath().substring(8))));
        }
    }

    public static void remapBlockEntity(MissingMappingsEvent.Mapping<BlockEntityType<?>> mapping) {
        var key = mapping.getKey();
    }
}
