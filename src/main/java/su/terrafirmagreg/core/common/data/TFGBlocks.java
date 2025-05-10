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

@SuppressWarnings("unused")
public final class TFGBlocks {

	// Reference table builders
	static ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> BUD_BLOCKS_BUILDER = ImmutableMap.builder();

	// Reference tables
	public static Map<Material, BlockEntry<BudIndicator>> BUD_BLOCKS;

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFGCore.MOD_ID);

	public static final RegistryObject<Block> WOOD_ASH_PILE = register("ash_pile",
		() -> new WoodAshPileBlock(BlockBehaviour.Properties.of()
		   .mapColor(MapColor.COLOR_LIGHT_GRAY)
		   .strength(0.2f)
		   .sound(SoundType.SAND)
		   .pushReaction(PushReaction.DESTROY)
		   .isViewBlocking((state, level, pos) -> state.getValue(WoodAshPileBlock.LAYERS) >= 8)
		   .isSuffocating((state, level, pos) -> state.getValue(WoodAshPileBlock.LAYERS) >= 8)));

	public static final RegistryObject<Block> LUNAR_ROOTS = register("lunar_roots",
		() -> new LunarRootsBlock(BlockBehaviour.Properties.of()
		    .mapColor(MapColor.NONE)
		    .strength(0.1f)
		    .sound(SoundType.NETHER_WART)
		    .pushReaction(PushReaction.DESTROY)
		    .lightLevel((state) -> 6)
		    .noCollission()
		    .noOcclusion()
		    .isViewBlocking((state, level, pos) -> false)
		    .isSuffocating((state, level, pos) -> false)));

	public static final RegistryObject<Block> LUNAR_SPROUTS = register("lunar_sprouts",
		() -> new LunarSproutsBlock(BlockBehaviour.Properties.of()
		    .mapColor(MapColor.NONE)
		    .strength(0.1f)
		    .sound(SoundType.NETHER_WART)
		    .pushReaction(PushReaction.DESTROY)
			.noCollission()
			.noOcclusion()
		    .isViewBlocking((state, level, pos) -> false)
		    .isSuffocating((state, level, pos) -> false)));

	public static final RegistryObject<Block> LUNAR_CHORUS_PLANT = register("lunar_chorus_plant",
		() -> new LunarChorusPlantBlock(BlockBehaviour.Properties.of()
		    .mapColor(MapColor.TERRACOTTA_PURPLE)
			.pushReaction(PushReaction.DESTROY)
		    .strength(0.2f)
		    .sound(SoundType.CHERRY_WOOD)));

	public static final RegistryObject<Block> LUNAR_CHORUS_FLOWER = register("lunar_chorus_flower",
		() -> new LunarChorusFlowerBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PURPLE)
			.pushReaction(PushReaction.DESTROY)
			.strength(0.2f)
			.sound(SoundType.CHERRY_WOOD),
		LUNAR_CHORUS_PLANT));


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

	@SuppressWarnings("deprecation")
	private static void registerBudIndicator(Material material, GTRegistrate registrate, ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> builder)
	{
		TagPrefix budTag;
		int lightLevel;

		var entry = registrate
			.block(material.getName() + "_bud_indicator", p -> new BudIndicator(p, material))
			.initialProperties(() -> Blocks.AMETHYST_CLUSTER)
			.properties(p -> p.noLootTable().noOcclusion().noCollission().strength(0.25f).lightLevel(b -> 3))
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
			ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(),
				"blocks/" + blockEntry.getId().getPath());
			((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
		});
	}

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
