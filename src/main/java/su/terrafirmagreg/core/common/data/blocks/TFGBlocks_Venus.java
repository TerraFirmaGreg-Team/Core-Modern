package su.terrafirmagreg.core.common.data.blocks;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBlocks_Venus {
	public static void init() {
	}

	private static final String[] fluorapatiteColors = {
		"white", "yellow", "orange", "brown", "green", "blue"
	};

	public static BlockEntry<Block> WHITE_FLUORAPATITE_SAND = createSand("white", MapColor.QUARTZ);
	public static BlockEntry<Block> YELLOW_FLUORAPATITE_SAND = createSand("yellow", MapColor.COLOR_YELLOW);
	public static BlockEntry<Block> ORANGE_FLUORAPATITE_SAND = createSand("orange", MapColor.COLOR_ORANGE);
	public static BlockEntry<Block> BROWN_FLUORAPATITE_SAND = createSand("brown", MapColor.COLOR_BROWN);
	public static BlockEntry<Block> GREEN_FLUORAPATITE_SAND = createSand("green", MapColor.COLOR_LIGHT_GREEN);
	public static BlockEntry<Block> BLUE_FLUORAPATITE_SAND = createSand("blue", MapColor.COLOR_LIGHT_BLUE);

	public static BlockEntry<Block> WHITE_FLUORAPATITE_SANDSTONE = createSandstone("white", MapColor.QUARTZ);
	public static BlockEntry<Block> YELLOW_FLUORAPATITE_SANDSTONE = createSandstone("yellow", MapColor.COLOR_YELLOW);
	public static BlockEntry<Block> ORANGE_FLUORAPATITE_SANDSTONE = createSandstone("orange", MapColor.COLOR_ORANGE);
	public static BlockEntry<Block> BROWN_FLUORAPATITE_SANDSTONE = createSandstone("brown", MapColor.COLOR_BROWN);
	public static BlockEntry<Block> GREEN_FLUORAPATITE_SANDSTONE = createSandstone("green", MapColor.COLOR_LIGHT_GREEN);
	public static BlockEntry<Block> BLUE_FLUORAPATITE_SANDSTONE = createSandstone("blue", MapColor.COLOR_LIGHT_BLUE);


	private static BlockEntry<Block> createSand(String color, MapColor mapColor)
	{
		return TFGCore.REGISTRATE.block("sand/fluorapatite/" + color, Block::new)
				   .initialProperties(() -> Blocks.SAND)
				   .properties(p -> p
										.mapColor(mapColor))
				   .tag(TFCTags.Blocks.CAN_LANDSLIDE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, Tags.Blocks.SAND)
				   .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
				   .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
				   .tag(Tags.Items.SAND, TFGTags.Items.FLUORAPATITE_SAND)
				   .build()
				   .register();
	}

	private static BlockEntry<Block> createSandstone(String color, MapColor mapColor)
	{
		return TFGCore.REGISTRATE.block("sandstone/raw/fluorapatite/" + color, Block::new)
				   .initialProperties(() -> Blocks.SANDSTONE)
				   .properties(p -> p
										.mapColor(mapColor)
										.strength(0.8f)
										.explosionResistance(0.8f))
				   .tag(BlockTags.MINEABLE_WITH_PICKAXE, Tags.Blocks.SANDSTONE)

				   .blockstate((ctx, prov) -> {
					   prov.horizontalBlock(ctx.getEntry(),
						   TFGCore.id("venus/sandstone_bottom_fluorapatite_" + color),
						   TFGCore.id("venus/sandstone_bottom_fluorapatite_" + color),
						   TFGCore.id("venus/sandstone_top_fluorapatite_" + color));
				   })
				   .item(BlockItem::new)
				   .tag(Tags.Items.SANDSTONE, TFGTags.Items.FLUORAPATITE_SANDSTONE)
				   .build()
				   .register();
	}
}
