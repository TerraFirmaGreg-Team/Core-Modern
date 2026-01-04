package su.terrafirmagreg.core.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
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
        public static final TagKey<Item> WraptorFood = createItemTag("tfg:wraptor_food");
        public static final TagKey<Item> EmptySyringe = createItemTag("tfg:empty_dna_syringes");

        public static final TagKey<Item> InsulatingContainer = createItemTag("tfg:insulating_container");
        public static final TagKey<Item> HotProtectionEquipment = createItemTag("tfg:hot_protection_equipment");
        public static final TagKey<Item> ColdProtectionEquipment = createItemTag("tfg:cold_protection_equipment");
        public static final TagKey<Item> FloatingProtectionEquipment = createItemTag("tfg:floating_protection_equipment");

        public static final TagKey<Item> AutoEatBlacklist = createItemTag("tfg:auto_eat_blacklist");

        //Block Interaction tags for use in EMI
        public static final TagKey<Item> INTERACTIONBRICK = createItemTag("tfg:interaction/brick");
        public static final TagKey<Item> INTERACTIONBRICKSTAIR = createItemTag("tfg:interaction/brick_stairs");
        public static final TagKey<Item> INTERACTIONBRICKSLAB = createItemTag("tfg:interaction/brick_slab");
        public static final TagKey<Item> INTERACTIONBRICKWALL = createItemTag("tfg:interaction/brick_wall");
        public static final TagKey<Item> INTERACTIONCRACKEDBRICK = createItemTag("tfg:interaction/cracked_brick");
        public static final TagKey<Item> INTERACTIONCRACKEDSTAIR = createItemTag("tfg:interaction/cracked_brick_stairs");
        public static final TagKey<Item> INTERACTIONCRACKEDSLAB = createItemTag("tfg:interaction/cracked_brick_slab");
        public static final TagKey<Item> INTERACTIONCRACKEDWALL = createItemTag("tfg:interaction/cracked_brick_wall");
        public static final TagKey<Item> INTERACTIONMOSSYBRICK = createItemTag("tfg:interaction/mossy_brick");
        public static final TagKey<Item> INTERACTIONMOSSYSTAIR = createItemTag("tfg:interaction/mossy_brick_stairs");
        public static final TagKey<Item> INTERACTIONMOSSYSLAB = createItemTag("tfg:interaction/mossy_brick_slab");
        public static final TagKey<Item> INTERACTIONMOSSYWALL = createItemTag("tfg:interaction/mossy_brick_wall");

        public static final TagKey<Item> INTERACTIONSMOOTHBRICK = createItemTag("tfg:interaction/smooth_brick");

        public static final TagKey<Item> INTERACTIONCOBBLE = createItemTag("tfg:interaction/cobble");
        public static final TagKey<Item> INTERACTIONCOBBLESTAIR = createItemTag("tfg:interaction/cobble_stairs");
        public static final TagKey<Item> INTERACTIONCOBBLESLAB = createItemTag("tfg:interaction/cobble_slab");
        public static final TagKey<Item> INTERACTIONCOBBLEWALL = createItemTag("tfg:interaction/cobble_wall");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLESTAIR = createItemTag("tfg:interaction/mossy_cobble_stairs");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLE = createItemTag("tfg:interaction/mossy_cobble");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLESLAB = createItemTag("tfg:interaction/mossy_cobble_slab");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLEWALL = createItemTag("tfg:interaction/mossy_cobble_wall");

        public static TagKey<Item> createItemTag(String path) {
            return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.parse(path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> GrassPlantableOn = createBlockTag("tfc:grass_plantable_on");
        public static final TagKey<Block> Logs = createBlockTag("minecraft:logs");
        public static final TagKey<Block> HarvesterHarvestable = createBlockTag("tfg:harvester_harvestable");
        public static final TagKey<Block> DoNotDestroyInSpace = createBlockTag("tfg:do_not_destroy_in_space");
        public static final TagKey<Block> HeightmapIgnore = createBlockTag("tfg:heightmap_ignore");
        public static final TagKey<Block> DecorativePlantAttachable = createBlockTag("tfg:decorative_plant_attachable");
        public static final TagKey<Block> TitaniumConcrete = createBlockTag("tfg:titanium_concrete");

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

    public static final class Biomes {
        // martian dust storm intensity
        public static final TagKey<Biome> HasSevereDustStorms = createBiomeTag("tfg:has_severe_dust_storms");
        public static final TagKey<Biome> HasModerateDustStorms = createBiomeTag("tfg:has_moderate_dust_storms");
        public static final TagKey<Biome> HasMildDustStorms = createBiomeTag("tfg:has_mild_dust_storms");

        // martian dust storm wind color
        public static final TagKey<Biome> HasDarkSandWind = createBiomeTag("tfg:has_dark_sand_particles");
        public static final TagKey<Biome> HasMediumSandWind = createBiomeTag("tfg:has_medium_sand_particles");
        public static final TagKey<Biome> HasLightSandWind = createBiomeTag("tfg:has_light_sand_particles");

        private static TagKey<Biome> createBiomeTag(String path) {
            return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), ResourceLocation.parse(path));
        }
    }
}
