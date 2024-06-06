package su.terrafirmagreg.core.objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class TFGTags {

    public static final class Items {

        public static final TagKey<Item> Tongs = createItemTag("tfchotornot:tongs");
        public static final TagKey<Item> Strings = createItemTag("forge:string");

        public static TagKey<Item> createItemTag(String path) {
            return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> RawRockBlocks = createBlockTag("tfc:rock/raw");

        public static TagKey<Block> createBlockTag(String path) {
            return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(path));
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> IsTFCOverworld = createBiomeTag("tfg:is_tfc_overworld");

        public static TagKey<Biome> createBiomeTag(String path) {
            return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation(path));
        }
    }
}
