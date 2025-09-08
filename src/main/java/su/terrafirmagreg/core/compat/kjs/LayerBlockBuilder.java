package su.terrafirmagreg.core.compat.kjs;

import com.notenoughmail.kubejs_tfc.util.ResourceUtils;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.util.Lazy;
import su.terrafirmagreg.core.common.data.blocks.KubeJSLayerBlock;

public class LayerBlockBuilder extends BlockBuilder {

	public transient Supplier<ItemLike> itemSupplier;

	public LayerBlockBuilder(ResourceLocation i) {
		super(i);

		noCollision = false;
		hardness = 0.2f;
		fullBlock = false;
		opaque = true;
		soundType = SoundType.SAND;
		notSolid = true;
		viewBlocking = false;
		suffocating = false;

		mapColor(MapColor.NONE);
		tagBlock(ResourceLocation.fromNamespaceAndPath("minecraft", "mineable/shovel"));
	}

	@Info("Sets the item or block to use in the Jade tooltip. (example: 'ad_astra:moon_sand')")
	public LayerBlockBuilder existingItem(String id)
	{
		ResourceLocation rl = ResourceLocation.tryParse(id);
		itemSupplier = Lazy.of(() -> {
			var i = RegistryInfo.ITEM.getValue(rl);
			if (i != null) {
				return i;
			}
			else {
				return RegistryInfo.BLOCK.getValue(rl).asItem();
			}
		});

		return this;
	}


	@Override
	public KubeJSLayerBlock createObject() {
		return new KubeJSLayerBlock(itemSupplier, createProperties());
	}

	@Override
	public void generateDataJsons(DataJsonGenerator generator) {
		ResourceUtils.lootTable(b -> b.addPool(p -> { }), generator, this);
	}
}
