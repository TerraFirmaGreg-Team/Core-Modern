package su.terrafirmagreg.core.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobColorItemClientHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor colorProvider = (stack, tintIndex) -> {
            // Only tint layer 1 and 2. Layers 0 and 3+ are not tinted.
            if (tintIndex != 1 && tintIndex != 2)
                return 0xFFFFFF;

            // Applies color tints for layers 1 (base) and 2 (overlay).
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                if (stack.getTag().contains("mob_type")) {
                    String mobId = stack.getTag().getString("mob_type");
                    if (mobId.isEmpty())
                        return 0xFFFFFF;

                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
                    if (type == null)
                        return 0xFFFFFF;

                    SpawnEggItem egg = SpawnEggItem.byId(type);
                    if (egg != null) {
                        int eggColor = (tintIndex == 1) ? egg.getColor(0) : egg.getColor(1);
                        return eggColor & 0xFFFFFF;
                    }

                    // Fallback: Search all spawn eggs for a matching type.
                    for (var item : ForgeRegistries.ITEMS.getValues()) {
                        if (item instanceof SpawnEggItem se) {
                            try {
                                EntityType<?> eggType = se.getType(null);
                                if (eggType == type) {
                                    int eggColor = (tintIndex == 1) ? se.getColor(0) : se.getColor(1);
                                    return eggColor & 0xFFFFFF;
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
            return 0xFFFFFF;
        };

        // Register the color provider for the filled syringe item.
        event.register(colorProvider, TFGItems.FILLED_DNA_SYRINGE.get());
        event.register(colorProvider, TFGItems.PROGENITOR_CELLS.get());
        event.register(colorProvider, TFGItems.FISH_ROE.get());
    }
}
