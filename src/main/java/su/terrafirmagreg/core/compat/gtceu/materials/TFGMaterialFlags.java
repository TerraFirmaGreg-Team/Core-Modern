package su.terrafirmagreg.core.compat.gtceu.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;

public final class TFGMaterialFlags {

    public static final MaterialFlag HAS_TFC_TOOL = new MaterialFlag.Builder("has_tfc_tool").build();
    public static final MaterialFlag HAS_GT_TOOL = new MaterialFlag.Builder("has_gt_tool").build();
    public static final MaterialFlag HAS_TFC_ARMOR = new MaterialFlag.Builder("has_tfc_armor").build();
    public static final MaterialFlag HAS_TFC_UTILITY = new MaterialFlag.Builder("has_tfc_utility").build();
    public static final MaterialFlag CAN_BE_UNMOLDED = new MaterialFlag.Builder("can_be_unmolded").build();
    public static final MaterialFlag GENERATE_DOUBLE_INGOTS = new MaterialFlag.Builder("generate_double_ingots").build();
    public static final MaterialFlag GENERATE_FIRMALIFE_GREENHOUSE_BLOCKS = new MaterialFlag.Builder("generate_firmalife_greenhouse_blocks").build();
    public static final MaterialFlag HAS_SMALL_TFC_ORE = new MaterialFlag.Builder("has_small_tfc_ore").build();
    public static final MaterialFlag GENERATE_DUSTY_ORES = new MaterialFlag.Builder("generate_dusty_ores").build();

    public static final MaterialFlag GENERATE_BUZZSAW_BLADE = new MaterialFlag.Builder("generate_buzzsaw_blade").build();
    public static final MaterialFlag GENERATE_SCREWDRIVER_HEAD = new MaterialFlag.Builder("generate_screwdriver_head").build();
    public static final MaterialFlag GENERATE_DRILL_HEAD = new MaterialFlag.Builder("generate_drill_head").build();
    public static final MaterialFlag GENERATE_CHAINSAW_HEAD = new MaterialFlag.Builder("generate_chainsaw_head").build();
    public static final MaterialFlag GENERATE_WRENCH_HEAD = new MaterialFlag.Builder("generate_wrench_head").build();
    public static final MaterialFlag GENERATE_WIRE_CUTTER_HEAD = new MaterialFlag.Builder("generate_wire_cutter_head").build();
}
