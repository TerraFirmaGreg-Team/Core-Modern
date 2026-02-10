package su.terrafirmagreg.core.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
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
        event.getMappings(Registries.BLOCK, TFGCore.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().getNamespace().startsWith("casings/"))
                mapping.remap(ForgeRegistries.BLOCKS.getValue(TFGCore.id(mapping.getKey().getNamespace().substring(8))));
        });
    }
}
