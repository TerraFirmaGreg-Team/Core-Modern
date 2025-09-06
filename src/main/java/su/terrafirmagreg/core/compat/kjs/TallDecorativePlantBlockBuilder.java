package su.terrafirmagreg.core.compat.kjs;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;
import com.notenoughmail.kubejs_tfc.event.RegisterInteractionsEventJS;
import com.notenoughmail.kubejs_tfc.util.ResourceUtils;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.loot.LootTableEntry;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import su.terrafirmagreg.core.common.data.blocks.TallDecorativePlantBlock;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class TallDecorativePlantBlockBuilder extends ExtendedPropertiesBlockBuilder {

	public transient VoxelShape cachedShape;
	public transient Supplier<Item> preexistingItem;
	public transient int maxHeight;

	public TallDecorativePlantBlockBuilder(ResourceLocation i) {
		super(i);

		noCollision = true;
		hardness = 0;
		fullBlock = false;
		opaque = false;
		notSolid = true;
		renderType = "cutout";
		soundType = SoundType.GRASS;
		maxHeight = 2;

		mapColor(MapColor.NONE);
		tagBlock(ResourceLocation.fromNamespaceAndPath("minecraft", "mineable/hoe"));
	}

	@Info("Sets the 'block item' of this block to an existing item")
	public TallDecorativePlantBlockBuilder withPreexistingItem(ResourceLocation item) {
		itemBuilder = null;
		preexistingItem = Lazy.of(() -> RegistryInfo.ITEM.getValue(item));
		RegisterInteractionsEventJS.addBlockItemPlacement(preexistingItem, this);
		return this;
	}

	@Info("Sets how tall the plant is")
	public TallDecorativePlantBlockBuilder height(int height) {
		this.maxHeight = height;
		return this;
	}

	@HideFromJS
	public VoxelShape getShape() {
		if (customShape.isEmpty()) {
			return TallDecorativePlantBlock.DEFAULT_SHAPE;
		}
		if (cachedShape == null) {
			cachedShape = BlockBuilder.createShape(customShape);
		}
		return cachedShape;
	}

	@HideFromJS
	public Supplier<Item> itemSupplier() {
		if (preexistingItem != null) {
			return preexistingItem;
		} else if (itemBuilder != null) {
			return itemBuilder;
		} else {
			return null;
		}
	}

	@Override
	public TallDecorativePlantBlock createObject() {
		return new TallDecorativePlantBlock(
                createExtendedProperties().offsetType(BlockBehaviour.OffsetType.XZ),
				getShape(),
			maxHeight
		);
	}

	@Override
	public void generateDataJsons(DataJsonGenerator generator) {
		ResourceUtils.lootTable(b -> b.addPool(p -> {
			p.survivesExplosion();
			p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:knives")));
			p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:hoes")));
			p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:scythes")));
		}), generator, this);
	}

	private LootTableEntry lootEntryBase(String tag) {
		final JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:item");
		if (preexistingItem != null) {
			json.addProperty("name", preexistingItem.get().toString());
		}
		else {
			json.addProperty("name", itemBuilder.id.toString());
		}
		return new LootTableEntry(json)
				.addCondition(ResourceUtils.buildJson((condition) -> {
					condition.addProperty("condition", "minecraft:match_tool");
					condition.add("predicate", ResourceUtils.buildJson((predicate) -> predicate.addProperty("tag", tag)));
				}));
	}
}
