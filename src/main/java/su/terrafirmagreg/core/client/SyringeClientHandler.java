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

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SyringeClientHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor colorProvider = (stack, tintIndex) -> {
            // Only tint layer 1 and 2. Layer 0 is the base texture.
            if (tintIndex == 0)
                return 0xFFFFFF;

            // Applies color tints to filled dna syringes.
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                if (stack.getTag().contains("mob_type")) {
                    String mobId = stack.getTag().getString("mob_type");
                    if (mobId.isEmpty())
                        return 0xFFFFFF; // Fallback for if a player gets a blank item through cheats.

                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobId));
                    if (type == null) {
                        // unknown entity fallback to white.
                        return 0xFFFFFF;
                    }

                    // Get spawn egg via the static mapping.
                    SpawnEggItem egg = SpawnEggItem.byId(type);
                    if (egg != null) {
                        // egg.getColor(0) = base.
                        // egg.getColor(1) = overlay.
                        int eggColor = (tintIndex == 1) ? egg.getColor(0) : egg.getColor(1);
                        return eggColor & 0xFFFFFF; // strip alpha if present.
                    }

                    // If SpawnEggItem.byId returned null, fallback: try to find any spawn egg item that targets this
                    // type.
                    for (var item : ForgeRegistries.ITEMS.getValues()) {
                        if (item instanceof SpawnEggItem se) {
                            try {
                                // Use the egg's default type (NBT = null).
                                EntityType<?> eggType = se.getType(null);
                                if (eggType == type) {
                                    int eggColor = (tintIndex == 1) ? se.getColor(0) : se.getColor(1);
                                    return eggColor & 0xFFFFFF;
                                }
                            } catch (Throwable ignored) {
                                // If a modded spawn egg behaves oddly, just skip it :x
                            }
                        }
                    }
                }
            }

            // Final fallback: White.
            return 0xFFFFFF;
        };

        // Register the color provider for the filled syringe item.
        event.register(colorProvider, TFGItems.FILLED_DNA_SYRINGE.get());
    }
}
