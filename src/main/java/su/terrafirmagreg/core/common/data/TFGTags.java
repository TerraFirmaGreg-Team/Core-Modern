package su.terrafirmagreg.core.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;

public final class TFGTags {

    public static final class Items {

        public static final TagKey<Item> Casings = createItemTag("casings");

        public static final TagKey<Item> Chains = createItemTag(ResourceLocation.fromNamespaceAndPath("forge", "chains"));
        public static final TagKey<Item> Harvester = createItemTag("harvester");

        public static final TagKey<Item> CannotLaunchInRailgun = createItemTag("cannot_launch_in_railgun");
        public static final TagKey<Item> OreProspectorsCopper = createItemTag("tools/ore_prospectors/copper");
        public static final TagKey<Item> OreProspectorsBronze = createItemTag("tools/ore_prospectors/bronze");
        public static final TagKey<Item> OreProspectorsWroughtIron = createItemTag("tools/ore_prospectors/wrought_iron");
        public static final TagKey<Item> OreProspectorsSteel = createItemTag("tools/ore_prospectors/steel");
        public static final TagKey<Item> OreProspectorsBlackSteel = createItemTag("tools/ore_prospectors/black_steel");
        public static final TagKey<Item> OreProspectorsBlueSteel = createItemTag("tools/ore_prospectors/blue_steel");
        public static final TagKey<Item> OreProspectorsRedSteel = createItemTag("tools/ore_prospectors/red_steel");

        public static final TagKey<Item> MartianHerbivoreFoods = createItemTag("martian_herbivore_foods");
        public static final TagKey<Item> MartianPiscivoreFoods = createItemTag("martian_piscivore_foods");
        public static final TagKey<Item> EmptySyringe = createItemTag("empty_dna_syringes");

        public static final TagKey<Item> InsulatingContainer = createItemTag("insulating_container");
        public static final TagKey<Item> HotProtectionEquipment = createItemTag("hot_protection_equipment");
        public static final TagKey<Item> ColdProtectionEquipment = createItemTag("cold_protection_equipment");
        public static final TagKey<Item> FloatingProtectionEquipment = createItemTag("floating_protection_equipment");

        public static final TagKey<Item> AutoEatBlacklist = createItemTag("auto_eat_blacklist");
        public static final TagKey<Item> ArtisanTableInputs = createItemTag("artisan_table_inputs");
        public static final TagKey<Item> ArtisanTableTools = createItemTag("artisan_table_tools");

        public static final TagKey<Item> GreenhouseCasings = createItemTag("all_greenhouse_casings");
        public static final TagKey<Item> StainlessSteelGreenhouseCasings = createItemTag("stainless_steel_greenhouse_casings");
        public static final TagKey<Item> IronGreenhouseCasings = createItemTag("iron_greenhouse_casings");
        public static final TagKey<Item> CopperGreenhouseCasings = createItemTag("copper_greenhouse_casings");
        public static final TagKey<Item> TreatedWoodGreenhouseCasings = createItemTag("treated_wood_greenhouse_casings");

        public static final TagKey<Item> SEAL_FOOD = createItemTag("seal_food");
        public static final TagKey<Item> SLIME_FOOD = createItemTag("slime_food");
        public static final TagKey<Item> SLIME_BALL = createItemTag("slime_ball");

        public static final TagKey<Item> PrecisionFabricatorDippedItems = createItemTag("precision_fabricator_dipped_items");
        public static final TagKey<Item> PrecisionFabricatorHolderRods = createItemTag("precision_fabricator_holder_rods");

        public static final TagKey<Item> Explosives = createItemTag("explosives");
        public static final TagKey<Item> FirmalifeOvenTops = createItemTag("oven_tops");
        public static final TagKey<Item> ROAD_MARKING_STENCILS = createItemTag("road_marking_stencils");

        //Block Interaction tags for use in EMI
        public static final TagKey<Item> INTERACTIONBRICK = createItemTag("interaction/brick");
        public static final TagKey<Item> INTERACTIONBRICKSTAIR = createItemTag("interaction/brick_stairs");
        public static final TagKey<Item> INTERACTIONBRICKSLAB = createItemTag("interaction/brick_slab");
        public static final TagKey<Item> INTERACTIONBRICKWALL = createItemTag("interaction/brick_wall");
        public static final TagKey<Item> INTERACTIONCRACKEDBRICK = createItemTag("interaction/cracked_brick");
        public static final TagKey<Item> INTERACTIONCRACKEDSTAIR = createItemTag("interaction/cracked_brick_stairs");
        public static final TagKey<Item> INTERACTIONCRACKEDSLAB = createItemTag("interaction/cracked_brick_slab");
        public static final TagKey<Item> INTERACTIONCRACKEDWALL = createItemTag("interaction/cracked_brick_wall");
        public static final TagKey<Item> INTERACTIONMOSSYBRICK = createItemTag("interaction/mossy_brick");
        public static final TagKey<Item> INTERACTIONMOSSYSTAIR = createItemTag("interaction/mossy_brick_stairs");
        public static final TagKey<Item> INTERACTIONMOSSYSLAB = createItemTag("interaction/mossy_brick_slab");
        public static final TagKey<Item> INTERACTIONMOSSYWALL = createItemTag("interaction/mossy_brick_wall");

        public static final TagKey<Item> INTERACTIONSMOOTHBRICK = createItemTag("interaction/smooth_brick");

        public static final TagKey<Item> INTERACTIONCOBBLE = createItemTag("interaction/cobble");
        public static final TagKey<Item> INTERACTIONCOBBLESTAIR = createItemTag("interaction/cobble_stairs");
        public static final TagKey<Item> INTERACTIONCOBBLESLAB = createItemTag("interaction/cobble_slab");
        public static final TagKey<Item> INTERACTIONCOBBLEWALL = createItemTag("interaction/cobble_wall");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLESTAIR = createItemTag("interaction/mossy_cobble_stairs");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLE = createItemTag("interaction/mossy_cobble");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLESLAB = createItemTag("interaction/mossy_cobble_slab");
        public static final TagKey<Item> INTERACTIONMOSSYCOBBLEWALL = createItemTag("interaction/mossy_cobble_wall");

        public static final TagKey<Item> GIRDER = createItemTag("girder");
        public static final TagKey<Item> STRUT = createItemTag("strut");

        public static final TagKey<Item> DYNAMIC_COLOR = createItemTag("dynamic_color");

        private static TagKey<Item> createItemTag(String path) {
            return createItemTag(TFGCore.id(path));
        }

        private static TagKey<Item> createItemTag(ResourceLocation resLoc) {
            return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), resLoc);
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> Casings = createBlockTag("casings");
        public static final TagKey<Block> HarvesterHarvestable = createBlockTag("harvester_harvestable");
        public static final TagKey<Block> DoNotDestroyInSpace = createBlockTag("do_not_destroy_in_space");
        public static final TagKey<Block> HeightmapIgnore = createBlockTag("heightmap_ignore");
        public static final TagKey<Block> DecorativePlantAttachable = createBlockTag("decorative_plant_attachable");
        public static final TagKey<Block> TitaniumConcrete = createBlockTag("titanium_concrete");
        public static final TagKey<Block> SolidLeaves = createBlockTag("solid_leaves");
        public static final TagKey<Block> GreenhouseCasings = createBlockTag("all_greenhouse_casings");
        public static final TagKey<Block> StainlessSteelGreenhouseCasings = createBlockTag("stainless_steel_greenhouse_casings");
        public static final TagKey<Block> IronGreenhouseCasings = createBlockTag("iron_greenhouse_casings");
        public static final TagKey<Block> CopperGreenhouseCasings = createBlockTag("copper_greenhouse_casings");
        public static final TagKey<Block> TreatedWoodGreenhouseCasings = createBlockTag("treated_wood_greenhouse_casings");

        public static final TagKey<Block> SeaStackRocks = createBlockTag("sea_stack_rocks");
        public static final TagKey<Block> DryPlantPlantableOn = createBlockTag("dry_plant_plantable_on");
        public static final TagKey<Block> EpiphytePlantableOn = createBlockTag("epiphyte_plantable_on");
        public static final TagKey<Block> AnemonePlantableOn = createBlockTag("anemone_plantable_on");
        public static final TagKey<Block> IsAnemone = createBlockTag("is_anemone");
        public static final TagKey<Block> NOT_SLOWED_WITH_SNOWSHOES = createBlockTag("not_slowed_with_snowshoes");

        public static final TagKey<Block> GIRDER = createBlockTag("girder");
        public static final TagKey<Block> PAVING_GIRDER = createBlockTag("paving_girder");
        public static final TagKey<Block> TRUSS = createBlockTag("truss");
        public static final TagKey<Block> STRUT = createBlockTag("strut");

        private static TagKey<Block> createBlockTag(String path) {
            return createBlockTag(TFGCore.id(path));
        }

        private static TagKey<Block> createBlockTag(ResourceLocation resLoc) {
            return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), resLoc);
        }
    }

    public static final class Fluids {
        public static final TagKey<Fluid> BreathableCompressedAir = createFluidTag("breathable_compressed_air");

        private static TagKey<Fluid> createFluidTag(String path) {
            return createFluidTag(TFGCore.id(path));
        }

        private static TagKey<Fluid> createFluidTag(ResourceLocation resLoc) {
            return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), resLoc);
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> IgnoresGravity = createEntityTag("ignores_gravity");
        public static final TagKey<EntityType<?>> IgnoresCacti = createEntityTag("ignores_cacti");
        public static final TagKey<EntityType<?>> NotRammedByRammers = createEntityTag("not_rammed_by_rammers");
        public static final TagKey<EntityType<?>> FishingNetScoopable = createEntityTag("fishing_net_scoopable");
        public static final TagKey<EntityType<?>> Genderless = createEntityTag("genderless");

        private static TagKey<EntityType<?>> createEntityTag(String path) {
            return createEntityTag(TFGCore.id(path));
        }

        private static TagKey<EntityType<?>> createEntityTag(ResourceLocation resLoc) {
            return TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), resLoc);
        }
    }

    public static final class Biomes {
        // martian dust storm intensity
        public static final TagKey<Biome> HasSevereDustStorms = createBiomeTag("has_severe_dust_storms");
        public static final TagKey<Biome> HasModerateDustStorms = createBiomeTag("has_moderate_dust_storms");
        public static final TagKey<Biome> HasMildDustStorms = createBiomeTag("has_mild_dust_storms");

        // martian dust storm wind color
        public static final TagKey<Biome> HasDarkSandWind = createBiomeTag("has_dark_sand_particles");
        public static final TagKey<Biome> HasMediumSandWind = createBiomeTag("has_medium_sand_particles");
        public static final TagKey<Biome> HasLightSandWind = createBiomeTag("has_light_sand_particles");

        // earth worldgen tags
        public static final TagKey<Biome> EarthIsCold = createBiomeTag("earth/is_cold");
        public static final TagKey<Biome> EarthIsDry = createBiomeTag("earth/is_dry");
        public static final TagKey<Biome> EarthIsFreshWater = createBiomeTag("earth/is_fresh_water");
        public static final TagKey<Biome> EarthIsHill = createBiomeTag("earth/is_hill");
        public static final TagKey<Biome> EarthIsIceSheet = createBiomeTag("earth/is_ice_sheet");
        public static final TagKey<Biome> EarthIsKarst = createBiomeTag("earth/is_karst");
        public static final TagKey<Biome> EarthIsLake = createBiomeTag("earth/is_lake");
        public static final TagKey<Biome> EarthIsMountain = createBiomeTag("earth/is_mountain");
        public static final TagKey<Biome> EarthIsNormal = createBiomeTag("earth/is_normal");
        public static final TagKey<Biome> EarthIsOcean = createBiomeTag("earth/is_ocean");
        public static final TagKey<Biome> EarthIsRiver = createBiomeTag("earth/is_river");
        public static final TagKey<Biome> EarthIsSaltFlats = createBiomeTag("earth/is_salt_flats");
        public static final TagKey<Biome> EarthIsSaltWater = createBiomeTag("earth/is_salt_water");
        public static final TagKey<Biome> EarthIsSandstone = createBiomeTag("earth/is_sandstone");
        public static final TagKey<Biome> EarthIsShoreIsland = createBiomeTag("earth/is_shore_island");
        public static final TagKey<Biome> EarthIsSwamp = createBiomeTag("earth/is_swamp");
        public static final TagKey<Biome> EarthIsTrueOcean = createBiomeTag("earth/is_true_ocean");
        public static final TagKey<Biome> EarthIsVolcanic = createBiomeTag("earth/is_volcanic");

        public static final TagKey<Biome> EarthIsOldGen = createBiomeTag("earth/is_oldgen");

        public static final TagKey<Biome> SlimeHabitat = createBiomeTag("slime_habitat");
        public static final TagKey<Biome> PlantSlimeHabitat = createBiomeTag("nether/plant_slime_habitat");
        public static final TagKey<Biome> GlowberrySlimeHabitat = createBiomeTag("nether/glowberry_slime_habitat");
        public static final TagKey<Biome> SpringSlimeHabitat = createBiomeTag("nether/spring_slime_habitat");
        public static final TagKey<Biome> IceSlimeHabitat = createBiomeTag("nether/ice_slime_habitat");
        public static final TagKey<Biome> LavaSlimeHabitat = createBiomeTag("nether/lava_slime_habitat");

        private static TagKey<Biome> createBiomeTag(String path) {
            return createBiomeTag(TFGCore.id(path));
        }

        private static TagKey<Biome> createBiomeTag(ResourceLocation resLoc) {
            return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), resLoc);
        }
    }
}
