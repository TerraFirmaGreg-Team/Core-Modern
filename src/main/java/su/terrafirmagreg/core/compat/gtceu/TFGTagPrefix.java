package su.terrafirmagreg.core.compat.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialIconType;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.oreTagPrefix;

public final class TFGTagPrefix {

    /* Stone Types */
    public static final TagPrefix oreGabbro;
    public static final TagPrefix oreShale;
    public static final TagPrefix oreClaystone;
    public static final TagPrefix oreLimestone;
    public static final TagPrefix oreConglomerate;
    public static final TagPrefix oreDolomite;
    public static final TagPrefix oreChert;
    public static final TagPrefix oreRhyolite;
    public static final TagPrefix oreDacite;
    public static final TagPrefix oreQuartzite;
    public static final TagPrefix oreSlate;
    public static final TagPrefix orePhyllite;
    public static final TagPrefix oreSchist;
    public static final TagPrefix oreGneiss;
    public static final TagPrefix oreMarble;
    public static final TagPrefix oreBasalt;
    public static final TagPrefix oreDiorite;
    public static final TagPrefix oreAndesite;
    public static final TagPrefix oreGranite;
    public static final TagPrefix oreChalk;

    /* Tool Heads */
    public static final TagPrefix toolHeadSword;
    public static final TagPrefix toolHeadPickaxe;
    public static final TagPrefix toolHeadShovel;
    public static final TagPrefix toolHeadAxe;
    public static final TagPrefix toolHeadHoe;
    public static final TagPrefix toolHeadScythe;
    public static final TagPrefix toolHeadFile;
    public static final TagPrefix toolHeadHammer;
    public static final TagPrefix toolHeadSaw;
    public static final TagPrefix toolHeadKnife;
    public static final TagPrefix toolHeadMiningHammer;
    public static final TagPrefix toolHeadButcheryKnife;
    public static final TagPrefix toolHeadSpade;
    public static final TagPrefix toolHeadPropick;
    public static final TagPrefix toolHeadJavelin;
    public static final TagPrefix toolHeadChisel;
    public static final TagPrefix toolHeadMace;

    /* Other */
    public static final TagPrefix ingotDouble;

    public static final TagPrefix poorRawOre;
    public static final TagPrefix richRawOre;

    public static final TagPrefix oreSmall;
    public static final TagPrefix oreSmallNative;

    public static final TagPrefix anvil;
    public static final TagPrefix lamp;
    public static final TagPrefix lampUnfinished;
    public static final TagPrefix trapdoor;
    public static final TagPrefix chain;
    public static final TagPrefix bell;
    public static final TagPrefix bars;

    public static final TagPrefix blockPlated;
    public static final TagPrefix stairPlated;
    public static final TagPrefix slabPlated;

    static {
        // Делаем все в статическом конструкторе
        // Для начала удаляем греговское дерьмо
        // После добавляем наши вкусные стоунтайпы

        TagPrefix.ORES.remove(TagPrefix.rawOreBlock);

        TagPrefix.ORES.remove(TagPrefix.ore);
        TagPrefix.ORES.remove(TagPrefix.oreDeepslate);
        TagPrefix.ORES.remove(TagPrefix.oreTuff);
        TagPrefix.ORES.remove(TagPrefix.oreSand);
        TagPrefix.ORES.remove(TagPrefix.oreRedSand);
        TagPrefix.ORES.remove(TagPrefix.oreRedGranite);
        TagPrefix.ORES.remove(TagPrefix.oreGravel);
        TagPrefix.ORES.remove(TagPrefix.oreEndstone);

        TagPrefix.ORES.remove(TagPrefix.oreBasalt);
        TagPrefix.ORES.remove(TagPrefix.oreAndesite);
        TagPrefix.ORES.remove(TagPrefix.oreDiorite);
        TagPrefix.ORES.remove(TagPrefix.oreGranite);

        /* Stone Types */
        oreGabbro = registerOreTagPrefix(Rock.GABBRO);
        oreShale = registerOreTagPrefix(Rock.SHALE);
        oreClaystone = registerOreTagPrefix(Rock.CLAYSTONE);
        oreLimestone = registerOreTagPrefix(Rock.LIMESTONE);
        oreConglomerate = registerOreTagPrefix(Rock.CONGLOMERATE);
        oreDolomite = registerOreTagPrefix(Rock.DOLOMITE);
        oreChert = registerOreTagPrefix(Rock.CHERT);
        oreRhyolite = registerOreTagPrefix(Rock.RHYOLITE);
        oreDacite = registerOreTagPrefix(Rock.DACITE);
        oreQuartzite = registerOreTagPrefix(Rock.QUARTZITE);
        oreSlate = registerOreTagPrefix(Rock.SLATE);
        orePhyllite = registerOreTagPrefix(Rock.PHYLLITE);
        oreSchist = registerOreTagPrefix(Rock.SCHIST);
        oreGneiss = registerOreTagPrefix(Rock.GNEISS);
        oreMarble = registerOreTagPrefix(Rock.MARBLE);
        oreBasalt = registerOreTagPrefix(Rock.BASALT);
        oreDiorite = registerOreTagPrefix(Rock.DIORITE);
        oreAndesite = registerOreTagPrefix(Rock.ANDESITE);
        oreGranite = registerOreTagPrefix(Rock.GRANITE);
        oreChalk = registerOreTagPrefix(Rock.CHALK);

        /* Tool Heads */
        toolHeadSword = new TagPrefix("swordHead")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .defaultTagPath("sword_heads/%s")
                .unformattedTagPath("sword_heads")
                .materialAmount(GTValues.M * 2)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadSword)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SWORD)));

        toolHeadPickaxe = new TagPrefix("pickaxeHead")
                .defaultTagPath("pickaxe_heads/%s")
                .unformattedTagPath("pickaxe_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadPickaxe)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.PICKAXE)));

        toolHeadShovel = new TagPrefix("shovelHead")
                .defaultTagPath("shovel_heads/%s")
                .unformattedTagPath("shovel_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadShovel)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SHOVEL)));

        toolHeadAxe = new TagPrefix("axeHead")
                .defaultTagPath("axe_heads/%s")
                .unformattedTagPath("axe_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadAxe)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.AXE)));

        toolHeadHoe = new TagPrefix("hoeHead")
                .defaultTagPath("hoe_heads/%s")
                .unformattedTagPath("hoe_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadHoe)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.HOE)));

        toolHeadScythe = new TagPrefix("scytheHead")
                .defaultTagPath("scythe_heads/%s")
                .unformattedTagPath("scythe_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadScythe)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SCYTHE)));

        toolHeadFile = new TagPrefix("fileHead")
                .defaultTagPath("file_heads/%s")
                .unformattedTagPath("file_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadFile)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.FILE)));

        toolHeadHammer = new TagPrefix("hammerHead")
                .defaultTagPath("hammer_heads/%s")
                .unformattedTagPath("hammer_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadHammer)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.HARD_HAMMER)));

        toolHeadSaw = new TagPrefix("sawHead")
                .defaultTagPath("saw_heads/%s")
                .unformattedTagPath("saw_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(MaterialIconType.toolHeadSaw)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SAW)));

        toolHeadKnife = new TagPrefix("knifeHead")
                .defaultTagPath("knife_heads/%s")
                .unformattedTagPath("knife_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadKnife)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.KNIFE)));

        toolHeadButcheryKnife = new TagPrefix("butcheryKnifeHead")
                .defaultTagPath("butchery_knife_heads/%s")
                .unformattedTagPath("butchery_knife_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M * 2)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadButcheryKnife)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.BUTCHERY_KNIFE)));

        toolHeadMiningHammer = new TagPrefix("miningHammerHead")
                .defaultTagPath("mining_hammer_heads/%s")
                .unformattedTagPath("mining_hammer_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M * 2)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadMiningHammer)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.MINING_HAMMER)));

        toolHeadSpade = new TagPrefix("spadeHead")
                .defaultTagPath("spade_heads/%s")
                .unformattedTagPath("spade_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M * 2)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadSpade)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SPADE)));

        toolHeadPropick = new TagPrefix("propickHead")
                .defaultTagPath("propick_heads/%s")
                .unformattedTagPath("propick_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadPropick)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        toolHeadJavelin = new TagPrefix("javelinHead")
                .defaultTagPath("javelin_heads/%s")
                .unformattedTagPath("javelin_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadJavelin)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        toolHeadChisel = new TagPrefix("chiselHead")
                .defaultTagPath("chisel_heads/%s")
                .unformattedTagPath("chisel_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadChisel)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        toolHeadMace = new TagPrefix("maceHead")
                .defaultTagPath("mace_heads/%s")
                .unformattedTagPath("mace_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M * 2)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadMace)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        /* Other */
        ingotDouble = new TagPrefix("doubleIngot")
                .defaultTagPath("double_ingots/%s")
                .unformattedTagPath("double_ingots")
                .materialAmount(GTValues.M)
                .materialIconType(MaterialIconType.ingotDouble)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.GENERATE_DOUBLE_INGOTS));

        poorRawOre = new TagPrefix("poor_raw", true)
                .idPattern("poor_raw_%s")
                .defaultTagPath("poor_raw_materials/%s")
                .unformattedTagPath("poor_raw_materials")
                .materialIconType(TFGMaterialIconType.poorRawOre)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasOreProperty);

        richRawOre = new TagPrefix("rich_raw", true)
                .idPattern("rich_raw_%s")
                .defaultTagPath("rich_raw_materials/%s")
                .unformattedTagPath("rich_raw_materials")
                .materialIconType(TFGMaterialIconType.richRawOre)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasOreProperty);

        oreSmall = new TagPrefix("smallOre")
                .materialAmount(GTValues.M / 4)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_SMALL_TFC_ORE));

        oreSmallNative = new TagPrefix("smallNativeOre")
                .materialAmount(GTValues.M / 4)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_SMALL_NATIVE_TFC_ORE));

        anvil = new TagPrefix("anvil")
                .materialAmount(GTValues.M * 14)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        lamp = new TagPrefix("lamp")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        lampUnfinished = new TagPrefix("unfinishedLamp")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        trapdoor = new TagPrefix("trapdoor")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        chain = new TagPrefix("chain")
                .materialAmount(GTValues.M / 16)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        bars = new TagPrefix("bars")
                .materialAmount(GTValues.M / 9)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_UTILITY));

        bell = new TagPrefix("bell")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.GENERATE_BELL));

        blockPlated = new TagPrefix("plated_block")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_PLATED_BLOCK));

        stairPlated = new TagPrefix("plated_stair")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_PLATED_BLOCK));

        slabPlated = new TagPrefix("plated_slab")
                .materialAmount(GTValues.M)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_PLATED_BLOCK));

        /* Tag Fixes */
        TagPrefix.plate.defaultTagPath("sheets/%s");
        TagPrefix.plateDouble.defaultTagPath("double_sheets/%s");

        TagPrefix.frameGt.unformattedTagPath("frames");

        TagPrefix.wireGtSingle.defaultTagPath("single_wires/%s");
        TagPrefix.wireGtSingle.unformattedTagPath("single_wires");
        TagPrefix.wireGtDouble.defaultTagPath("double_wires/%s");
        TagPrefix.wireGtDouble.unformattedTagPath("double_wires");
        TagPrefix.wireGtQuadruple.defaultTagPath("quadruple_wires/%s");
        TagPrefix.wireGtQuadruple.unformattedTagPath("quadruple_wires");
        TagPrefix.wireGtOctal.defaultTagPath("octal_wires/%s");
        TagPrefix.wireGtOctal.unformattedTagPath("octal_wires");
        TagPrefix.wireGtHex.defaultTagPath("hex_wires/%s");
        TagPrefix.wireGtHex.unformattedTagPath("hex_wires");

        TagPrefix.cableGtSingle.defaultTagPath("single_cables/%s");
        TagPrefix.cableGtSingle.unformattedTagPath("single_cables");
        TagPrefix.cableGtDouble.defaultTagPath("double_cables/%s");
        TagPrefix.cableGtDouble.unformattedTagPath("double_cables");
        TagPrefix.cableGtQuadruple.defaultTagPath("quadruple_cables/%s");
        TagPrefix.cableGtQuadruple.unformattedTagPath("quadruple_cables");
        TagPrefix.cableGtOctal.defaultTagPath("octal_cables/%s");
        TagPrefix.cableGtOctal.unformattedTagPath("octal_cables");
        TagPrefix.cableGtHex.defaultTagPath("hex_cables/%s");
        TagPrefix.cableGtHex.unformattedTagPath("hex_cables");

        TagPrefix.pipeTinyFluid.defaultTagPath("tiny_fluid_pipes/%s");
        TagPrefix.pipeTinyFluid.unformattedTagPath("tiny_fluid_pipes");
        TagPrefix.pipeSmallFluid.defaultTagPath("small_fluid_pipes/%s");
        TagPrefix.pipeSmallFluid.unformattedTagPath("small_fluid_pipes");
        TagPrefix.pipeNormalFluid.defaultTagPath("normal_fluid_pipes/%s");
        TagPrefix.pipeNormalFluid.unformattedTagPath("normal_fluid_pipes");
        TagPrefix.pipeLargeFluid.defaultTagPath("large_fluid_pipes/%s");
        TagPrefix.pipeLargeFluid.unformattedTagPath("large_fluid_pipes");
        TagPrefix.pipeHugeFluid.defaultTagPath("huge_fluid_pipes/%s");
        TagPrefix.pipeHugeFluid.unformattedTagPath("huge_fluid_pipes");
        TagPrefix.pipeQuadrupleFluid.defaultTagPath("quadruple_fluid_pipes/%s");
        TagPrefix.pipeQuadrupleFluid.unformattedTagPath("quadruple_fluid_pipes");
        TagPrefix.pipeNonupleFluid.defaultTagPath("nonuple_fluid_pipes/%s");
        TagPrefix.pipeNonupleFluid.unformattedTagPath("nonuple_fluid_pipes");

        // FirmaLife Shit
        TagPrefix.pipeTinyFluid.customTagPath("", (prefix, mat) -> TFGTags.Items.createItemTag("firmalife:always_valid_greenhouse_wall"));
        TagPrefix.pipeSmallFluid.customTagPath("", (prefix, mat) -> TFGTags.Items.createItemTag("firmalife:always_valid_greenhouse_wall"));
        TagPrefix.pipeNormalFluid.customTagPath("", (prefix, mat) -> TFGTags.Items.createItemTag("firmalife:always_valid_greenhouse_wall"));
        TagPrefix.pipeLargeFluid.customTagPath("", (prefix, mat) -> TFGTags.Items.createItemTag("firmalife:always_valid_greenhouse_wall"));
        // FirmaLife Shit

        TagPrefix.pipeSmallItem.defaultTagPath("small_item_pipes/%s");
        TagPrefix.pipeSmallItem.unformattedTagPath("small_item_pipes");
        TagPrefix.pipeNormalItem.defaultTagPath("normal_item_pipes/%s");
        TagPrefix.pipeNormalItem.unformattedTagPath("normal_item_pipes");
        TagPrefix.pipeLargeItem.defaultTagPath("large_item_pipes/%s");
        TagPrefix.pipeLargeItem.unformattedTagPath("large_item_pipes");
        TagPrefix.pipeHugeItem.defaultTagPath("huge_item_pipes/%s");
        TagPrefix.pipeHugeItem.unformattedTagPath("huge_item_pipes");

        TagPrefix.pipeSmallRestrictive.defaultTagPath("small_restrictive_pipes/%s");
        TagPrefix.pipeSmallRestrictive.unformattedTagPath("small_restrictive_pipes");
        TagPrefix.pipeNormalRestrictive.defaultTagPath("normal_restrictive_pipes/%s");
        TagPrefix.pipeNormalRestrictive.unformattedTagPath("normal_restrictive_pipes");
        TagPrefix.pipeLargeRestrictive.defaultTagPath("large_restrictive_pipes/%s");
        TagPrefix.pipeLargeRestrictive.unformattedTagPath("large_restrictive_pipes");
        TagPrefix.pipeHugeRestrictive.defaultTagPath("huge_restrictive_pipes/%s");
        TagPrefix.pipeHugeRestrictive.unformattedTagPath("huge_restrictive_pipes");

        TagPrefix.toolHeadDrill.defaultTagPath("drill_heads/%s");
        TagPrefix.toolHeadDrill.unformattedTagPath("drill_heads");

        TagPrefix.toolHeadChainsaw.defaultTagPath("chainsaw_heads/%s");
        TagPrefix.toolHeadChainsaw.unformattedTagPath("chainsaw_heads");

        TagPrefix.toolHeadBuzzSaw.defaultTagPath("buzz_saw_heads/%s");
        TagPrefix.toolHeadBuzzSaw.unformattedTagPath("buzz_saw_heads");

        TagPrefix.toolHeadWrench.defaultTagPath("wrench_tips/%s");
        TagPrefix.toolHeadWrench.unformattedTagPath("wrench_tips");

        TagPrefix.toolHeadScrewdriver.defaultTagPath("screwdriver_tips/%s");
        TagPrefix.toolHeadScrewdriver.unformattedTagPath("screwdriver_tips");

        TagPrefix.turbineBlade.defaultTagPath("turbine_blades/%s");
        TagPrefix.turbineBlade.unformattedTagPath("turbine_blades");
    }

    private static TagPrefix registerOreTagPrefix(Rock rockType) {
        var material = GTCEuAPI.materialManager.getMaterial(TFGCore.MOD_ID + ":" + rockType.getSerializedName());
        if (material == null) {
            material = GTCEuAPI.materialManager.getMaterial(rockType.getSerializedName());
        }

        if (material == null) throw new IllegalArgumentException("Bad material in ore generation for: " + rockType.getSerializedName());

        final Material fMaterial = material;
        var tag = oreTagPrefix(rockType.getSerializedName(), BlockTags.MINEABLE_WITH_PICKAXE)
            .registerOre(
                    () -> TFCBlocks.ROCK_BLOCKS.get(rockType).get(Rock.BlockType.RAW).orElse(Blocks.STONE).defaultBlockState(),
                    () -> fMaterial,
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F),
                    new ResourceLocation(TerraFirmaCraft.MOD_ID, "block/rock/raw/" + rockType.getSerializedName())
            );
        tag.addSecondaryMaterial(new MaterialStack(material, GTValues.M));

        return tag;
    }

    public static void init() {}
}
