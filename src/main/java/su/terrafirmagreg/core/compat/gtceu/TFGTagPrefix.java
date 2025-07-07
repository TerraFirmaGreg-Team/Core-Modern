package su.terrafirmagreg.core.compat.gtceu;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialIconType;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.*;

public final class TFGTagPrefix {

    // These must stay in java because they don't work correctly in KJS -Py

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
    public static final TagPrefix toolHeadMattock;

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

        toolHeadMattock = new TagPrefix("mattockHead")
                .defaultTagPath("mattock_heads/%s")
                .unformattedTagPath("mattock_heads")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadMattock)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        toolHeadHook = new TagPrefix("fishHook")
                .defaultTagPath("fish_hooks/%s")
                .unformattedTagPath("fish_hooks")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .maxStackSize(16)
                .materialIconType(TFGMaterialIconType.toolHeadHook)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL));

        /* Other */
        ingotDouble = new TagPrefix("doubleIngot")
                .defaultTagPath("double_ingots/%s")
                .unformattedTagPath("double_ingots")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .materialAmount(GTValues.M)
                .materialIconType(MaterialIconType.ingotDouble)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(mat -> mat.hasFlag(TFGMaterialFlags.GENERATE_DOUBLE_INGOTS));

        poorRawOre = new TagPrefix("poor_raw", true)
                .idPattern("poor_raw_%s")
                .defaultTagPath("poor_raw_materials/%s")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
                .unformattedTagPath("poor_raw_materials")
                .materialIconType(TFGMaterialIconType.poorRawOre)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasOreProperty);

        richRawOre = new TagPrefix("rich_raw", true)
                .idPattern("rich_raw_%s")
                .defaultTagPath("rich_raw_materials/%s")
                .itemTable(() -> GTMaterialItems.MATERIAL_ITEMS)
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
                .defaultTagPath("chains/%s")
                .unformattedTagPath("chains")
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
    }

    public static void init() {}
}
