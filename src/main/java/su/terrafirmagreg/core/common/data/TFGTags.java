package su.terrafirmagreg.core.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public final class TFGTags {

    public static final class Items {
        public static final TagKey<Item> Hammers = createItemTag("forge:tools/hammers");
        public static final TagKey<Item> Chains = createItemTag("forge:chains");
        public static final TagKey<Item> Strings = createItemTag("forge:string");
        public static final TagKey<Item> Harvester = createItemTag("tfg:harvester");
        public static final TagKey<Item> CannotLaunchInRailgun = createItemTag("tfg:cannot_launch_in_railgun");
        public static final TagKey<Item> OreProspectorsCopper = createItemTag("tfg:tools/ore_prospectors/copper");
        public static final TagKey<Item> OreProspectorsBronze = createItemTag("tfg:tools/ore_prospectors/bronze");
        public static final TagKey<Item> OreProspectorsWroughtIron = createItemTag("tfg:tools/ore_prospectors/wrought_iron");
        public static final TagKey<Item> OreProspectorsSteel = createItemTag("tfg:tools/ore_prospectors/steel");
        public static final TagKey<Item> OreProspectorsBlackSteel = createItemTag("tfg:tools/ore_prospectors/black_steel");
        public static final TagKey<Item> OreProspectorsBlueSteel = createItemTag("tfg:tools/ore_prospectors/blue_steel");
        public static final TagKey<Item> OreProspectorsRedSteel = createItemTag("tfg:tools/ore_prospectors/red_steel");
        public static final TagKey<Item> GlacianRamFood = createItemTag("tfg:glacian_ram_food");
        public static final TagKey<Item> SnifferFood = createItemTag("tfg:sniffer_food");


        public static TagKey<Item> createItemTag(String path) {
            return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.parse(path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> GrassPlantableOn = createBlockTag("tfc:grass_plantable_on");
        public static final TagKey<Block> Logs = createBlockTag("minecraft:logs");
        public static final TagKey<Block> HarvesterHarvestable = createBlockTag("tfg:harvester_harvestable");
        public static final TagKey<Block> DoNotDestroyInSpace = createBlockTag("tfg:do_not_destroy_in_space");

        public static TagKey<Block> createBlockTag(String path) {
            return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.parse(path));
        }
    }

    public static final class Fluids {
        public static final TagKey<Fluid> BreathableCompressedAir = createFluidTag("tfg:breathable_compressed_air");

        public static TagKey<Fluid> createFluidTag(String path) {
            return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.parse(path));
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> IgnoresGravity = createEntityTag("tfg:ignores_gravity");

        public static TagKey<EntityType<?>> createEntityTag(String path) {
            return TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), ResourceLocation.parse(path));
        }
    }


}
