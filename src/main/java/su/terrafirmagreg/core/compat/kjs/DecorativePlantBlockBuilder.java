package su.terrafirmagreg.core.compat.kjs;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;
import com.notenoughmail.kubejs_tfc.event.RegisterInteractionsEventJS;
import com.notenoughmail.kubejs_tfc.util.ResourceUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.loot.LootTableEntry;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;

import su.terrafirmagreg.core.common.data.blocks.DecorativePlantBlock;

@SuppressWarnings("unused")
public class DecorativePlantBlockBuilder extends ExtendedPropertiesBlockBuilder {

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    @Nullable
    public transient String lootItem;

    public DecorativePlantBlockBuilder(ResourceLocation i) {
        super(i);

        noCollision = true;
        hardness = 0;
        fullBlock = false;
        opaque = false;
        notSolid = true;
        renderType = "cutout";
        soundType = SoundType.GRASS;

        mapColor(MapColor.NONE);
        tagBlock(ResourceLocation.fromNamespaceAndPath("minecraft", "mineable/hoe"));
    }

    @Info("Sets the 'block item' of this block to an existing item")
    public DecorativePlantBlockBuilder withPreexistingItem(ResourceLocation item) {
        itemBuilder = null;
        preexistingItem = Lazy.of(() -> RegistryInfo.ITEM.getValue(item));
        RegisterInteractionsEventJS.addBlockItemPlacement(preexistingItem, this);
        return this;
    }

    @Info("Whether or not this block should break into straw when cut with a hoe")
    public DecorativePlantBlockBuilder lootItem(String item) {
        lootItem = item;
        return this;
    }

    @HideFromJS
    public VoxelShape getShape() {
        if (customShape.isEmpty()) {
            return DecorativePlantBlock.DEFAULT_SHAPE;
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
    public DecorativePlantBlock createObject() {
        return new DecorativePlantBlock(createExtendedProperties().offsetType(BlockBehaviour.OffsetType.XZ),
                getShape());
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        ResourceUtils.lootTable(b -> b.addPool(p -> {
            p.survivesExplosion();
            p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:knives", false)));
            p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:hoes", false)));
            p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:scythes", false)));
            p.addEntry(ResourceUtils.alternatives(lootEntryBase("tfc:shears", true)));
        }), generator, this);
    }

    private LootTableEntry lootEntryBase(String tag, boolean silkTouch) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:item");
        if (lootItem != null && !silkTouch) {
            json.addProperty("name", lootItem);
        } else if (preexistingItem != null) {
            json.addProperty("name", preexistingItem.get().toString());
        } else {
            json.addProperty("name", itemBuilder.id.toString());
        }
        return new LootTableEntry(json)
                .addCondition(ResourceUtils.buildJson((condition) -> {
                    condition.addProperty("condition", "minecraft:match_tool");
                    condition.add("predicate",
                            ResourceUtils.buildJson((predicate) -> predicate.addProperty("tag", tag)));
                }));
    }
}
