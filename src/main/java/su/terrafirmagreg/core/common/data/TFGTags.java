package su.terrafirmagreg.core.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class TFGTags {

    public static final class Items {
        public static final TagKey<Item> Hammers = createItemTag("forge:tools/hammers");

        public static final TagKey<Item> Strings = createItemTag("forge:string");

        public static TagKey<Item> createItemTag(String path) {
            return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> GrassPlantableOn = createBlockTag("tfc:grass_plantable_on");
        public static final TagKey<Block> Logs = createBlockTag("minecraft:logs");

        public static TagKey<Block> createBlockTag(String path) {
            return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(path));
        }
    }
}
