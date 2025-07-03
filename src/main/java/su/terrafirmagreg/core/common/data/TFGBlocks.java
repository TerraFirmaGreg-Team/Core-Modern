package su.terrafirmagreg.core.common.data;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import earth.terrarium.adastra.common.registry.ModBlocks;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.*;
import su.terrafirmagreg.core.common.data.buds.BudIndicator;
import su.terrafirmagreg.core.common.data.buds.BudIndicatorItem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused"})
public final class TFGBlocks {

	// Reference table builders
	static ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> BUD_BLOCKS_BUILDER = ImmutableMap.builder();

	// Reference tables
	public static Map<Material, BlockEntry<BudIndicator>> BUD_BLOCKS;

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFGCore.MOD_ID);


	// "Layer" blocks like the charcoal pile

	private static final BlockBehaviour.Properties s_layerProperties = BlockBehaviour.Properties.of()
			.strength(0.2f)
			.sound(SoundType.SAND)
			.pushReaction(PushReaction.DESTROY)
			.isViewBlocking((state, level, pos) -> state.getValue(LayerBlock.LAYERS) >= 8)
			.isSuffocating((state, level, pos) -> state.getValue(LayerBlock.LAYERS) >= 8);

	// This one isn't in the pile folder for backwards compatibility
	public static final RegistryObject<Block> WOOD_ASH_PILE = register("ash_pile",
		() -> new LayerBlock(TFCItems.POWDERS.get(Powder.WOOD_ASH)::get, s_layerProperties.mapColor(MapColor.COLOR_LIGHT_GRAY)));

	public static final RegistryObject<Block> WHITE_SAND_PILE = register("pile/white_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.WHITE)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> BLACK_SAND_PILE = register("pile/black_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.BLACK)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> BROWN_SAND_PILE = register("pile/brown_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.BROWN)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> RED_SAND_PILE = register("pile/red_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.RED)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> YELLOW_SAND_PILE = register("pile/yellow_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.YELLOW)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> GREEN_SAND_PILE = register("pile/green_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.GREEN)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> PINK_SAND_PILE = register("pile/pink_sand",
		() -> new LayerBlock(TFCBlocks.SAND.get(SandBlockType.PINK)::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> MOON_SAND_PILE = register("pile/moon_sand",
		() -> new LayerBlock(ModBlocks.MOON_SAND::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> MARS_SAND_PILE = register("pile/mars_sand",
		() -> new LayerBlock(ModBlocks.MARS_SAND::get, s_layerProperties.mapColor(MapColor.NONE)));
	public static final RegistryObject<Block> VENUS_SAND_PILE = register("pile/venus_sand",
		() -> new LayerBlock(ModBlocks.VENUS_SAND::get, s_layerProperties.mapColor(MapColor.NONE)));


	// Decoration blocks

	public static final RegistryObject<Block> LUNAR_CHORUS_PLANT = register("lunar_chorus_plant",
		() -> new LunarChorusPlantBlock(BlockBehaviour.Properties.of()
		    .mapColor(MapColor.TERRACOTTA_PURPLE)
			.pushReaction(PushReaction.DESTROY)
			.noLootTable()
		    .strength(0.2f)
		    .sound(SoundType.CHERRY_WOOD)));

	public static final RegistryObject<Block> LUNAR_CHORUS_FLOWER = register("lunar_chorus_flower",
		() -> new LunarChorusFlowerBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PURPLE)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
			.strength(0.2f)
			.sound(SoundType.CHERRY_WOOD),
		LUNAR_CHORUS_PLANT));


	// Misc blocks

	public static final RegistryObject<Block> PIGLIN_DISGUISE_BLOCK = register("piglin_disguise_block",
		() -> new PiglinDisguiseBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BROWN)
			.strength(0.1f)
			.sound(SoundType.DRIPSTONE_BLOCK)
			.pushReaction(PushReaction.DESTROY)
			.isViewBlocking((state, level, pos) -> false)
			.isSuffocating((state, level, pos) -> false)));


	// Buds are generated automatically

	public static void generateBudIndicators()
	{
		BUD_BLOCKS_BUILDER = ImmutableMap.builder();

		for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries())
		{
			GTRegistrate registrate = registry.getRegistrate();
			for (Material material : registry.getAllMaterials())
			{
				if (material.hasProperty(PropertyKey.ORE) && material.hasProperty(PropertyKey.GEM))
				{
					registerBudIndicator(material, registrate, BUD_BLOCKS_BUILDER);
				}
			}
		}
		BUD_BLOCKS = BUD_BLOCKS_BUILDER.build();
	}

	@SuppressWarnings("removal")
	private static void registerBudIndicator(Material material, GTRegistrate registrate, ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> builder)
	{
		TagPrefix budTag;
		int lightLevel;

		var entry = registrate
			.block(material.getName() + "_bud_indicator", p -> new BudIndicator(p, material))
			.initialProperties(() -> Blocks.AMETHYST_CLUSTER)
			.properties(p -> p
				.noLootTable()
				.noOcclusion()
				.noCollission()
				.strength(0.25f)
				.lightLevel(b -> 3)
				.offsetType(BlockBehaviour.OffsetType.XZ))
			.setData(ProviderType.LANG, NonNullBiConsumer.noop())
			.setData(ProviderType.LOOT, NonNullBiConsumer.noop())
			.setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
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

	private static final VanillaBlockLoot BLOCK_LOOT = new VanillaBlockLoot();

	public static void generateBudIndicatorLoot(Map<ResourceLocation, LootTable> lootTables) {
		TFGBlocks.BUD_BLOCKS.forEach((material, blockEntry) -> {
			ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
				"blocks/" + blockEntry.getId().getPath());
			((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
		});
	}


	// Helper registration methods

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
	{
		return register(name, blockSupplier, b -> new BlockItem(b, new Item.Properties()));
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, Item.Properties blockItemProperties)
	{
		return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties));
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
	{
		return RegistrationHelpers.registerBlock(BLOCKS, TFGItems.ITEMS, name, blockSupplier, blockItemFactory);
	}
}
