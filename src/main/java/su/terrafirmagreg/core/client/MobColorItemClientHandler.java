package su.terrafirmagreg.core.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import su.terrafirmagreg.core.compat.starcatcher.StarcatcherFishVariants;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobColorItemClientHandler {

    private static final Map<String, Integer> COLOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> RESOURCE_LOCATION_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, EntityType<?>> ENTITY_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache mob color.
     */
    private static int getCachedMobColor(String mobId, int tintIndex) {
        String cacheKey = mobId + ":" + tintIndex;
        return COLOR_CACHE.computeIfAbsent(cacheKey, key -> computeMobColor(mobId, tintIndex));
    }

    /**
     * Compute the actual mob color when not cached.
     */
    private static int computeMobColor(String mobId, int tintIndex) {
        if (mobId.isEmpty()) {
            return 0xFFFFFF;
        }
        ResourceLocation resourceLocation = RESOURCE_LOCATION_CACHE.computeIfAbsent(mobId, id -> {
            try {
                return ResourceLocation.parse(id);
            } catch (Exception e) {
                return null;
            }
        });

        if (resourceLocation == null) {
            return 0xFFFFFF;
        }
        EntityType<?> type = ENTITY_TYPE_CACHE.computeIfAbsent(resourceLocation,
                ForgeRegistries.ENTITY_TYPES::getValue);

        if (type == null) {
            return 0xFFFFFF;
        }
        SpawnEggItem egg = SpawnEggItem.byId(type);
        if (egg != null) {
            int eggColor = (tintIndex == 1) ? egg.getColor(0) : egg.getColor(1);
            return eggColor & 0xFFFFFF;
        }

        // Fallback to search all spawn eggs.
        for (var item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof SpawnEggItem se) {
                try {
                    EntityType<?> eggType = se.getType(null);
                    if (eggType == type) {
                        int eggColor = (tintIndex == 1) ? se.getColor(0) : se.getColor(1);
                        return eggColor & 0xFFFFFF;
                    }
                } catch (Throwable ignored) {
                    // Ignore errors in spawn egg type retrieval.
                }
            }
        }

        return 0xFFFFFF;
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {

        StarcatcherFishVariants.initializeFishVariants();

        ItemColor colorProvider = (stack, tintIndex) -> {
            // Only tint layer 1 and 2. Layers 0 and 3+ are not tinted.
            if (tintIndex != 1 && tintIndex != 2)
                return 0xFFFFFF;

            // Applies color tints for layers 1 (base) and 2 (overlay).
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                if (stack.getTag().contains("mob_type")) {
                    // Check if this is a Starcatcher fish.
                    if (StarcatcherFishVariants.isStarcatcherFish(stack)) {
                        return StarcatcherFishVariants.getStarcatcherFishColor(stack, tintIndex);
                    }

                    String mobId = stack.getTag().getString("mob_type");

                    return getCachedMobColor(mobId, tintIndex);
                }
            }
            return 0xFFFFFF;
        };

        // Register the color provider for related items.
        event.register(colorProvider, TFGItems.FILLED_DNA_SYRINGE.get());
        event.register(colorProvider, TFGItems.PROGENITOR_CELLS.get());
        event.register(colorProvider, TFGItems.FISH_ROE.get());
    }
}
