package su.terrafirmagreg.core.common.data;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import su.terrafirmagreg.core.common.data.buds.BudIndicator;
import su.terrafirmagreg.core.common.data.buds.BudIndicatorItem;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;

public class TFGBlocks_Buds {
    public static void init() {
    }

    // Reference table builders
    static ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> BUD_BLOCKS_BUILDER = ImmutableMap.builder();

    // Reference tables
    public static Map<Material, BlockEntry<BudIndicator>> BUD_BLOCKS = new Object2ObjectOpenHashMap<>();

    // Buds are generated automatically

    public static void generateBudIndicators() {
        BUD_BLOCKS_BUILDER = ImmutableMap.builder();

        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (Material material : registry.getAllMaterials()) {
                if (material.hasProperty(PropertyKey.ORE) && material.hasProperty(PropertyKey.GEM)) {
                    registerBudIndicator(material, registrate, BUD_BLOCKS_BUILDER);
                }
            }
        }
        BUD_BLOCKS = BUD_BLOCKS_BUILDER.build();
    }

    @SuppressWarnings("removal")
    private static void registerBudIndicator(Material material, GTRegistrate registrate,
            ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> builder) {
        var entry = registrate
                .block("%s_bud_indicator".formatted(material.getName()), p -> new BudIndicator(p, material))
                .properties(p -> p
                        .mapColor(MapColor.NONE)
                        .noLootTable()
                        .noOcclusion()
                        .noCollission()
                        .strength(0.25f)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .sound(SoundType.AMETHYST_CLUSTER)
                        .pushReaction(PushReaction.DESTROY))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .transform(GTBlocks.unificationBlock(TFGTagPrefix.budIndicator, material))
                // "deprecated" but gregtech uses it too
                .addLayer(() -> RenderType::cutoutMipped)
                .color(() -> BudIndicator::tintedBlockColor)
                .item((b, p) -> BudIndicatorItem.create(b, p, material))
                .color(() -> BudIndicator::tintedItemColor)
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .build()
                .register();
        builder.put(material, entry);
    }

    public static void generateBudIndicatorLoot(Map<ResourceLocation, LootTable> lootTables) {
        BUD_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
        });
    }
}
