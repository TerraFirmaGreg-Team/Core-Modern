package su.terrafirmagreg.core.compat.gtceu.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;

public final class TFGMaterialFlags {

    public static final MaterialFlag HAS_TFC_TOOL = new MaterialFlag.Builder("has_tfc_tool").build();
    public static final MaterialFlag HAS_GT_TOOL = new MaterialFlag.Builder("has_gt_tool").build();
    public static final MaterialFlag HAS_TFC_ARMOR = new MaterialFlag.Builder("has_tfc_armor").build();
    public static final MaterialFlag HAS_TFC_UTILITY = new MaterialFlag.Builder("has_tfc_utility").build();
    public static final MaterialFlag HAS_PLATED_BLOCK = new MaterialFlag.Builder("has_plated_block").build();
    public static final MaterialFlag CAN_BE_UNMOLDED = new MaterialFlag.Builder("can_be_unmolded").build();
    public static final MaterialFlag GENERATE_BELL = new MaterialFlag.Builder("generate_bell").build();
    public static final MaterialFlag GENERATE_DOUBLE_INGOTS = new MaterialFlag.Builder("generate_double_ingots").build();
    public static final MaterialFlag GENERATE_FIRMALIFE_GREENHOUSE_BLOCKS = new MaterialFlag.Builder("generate_firmalife_greenhouse_blocks").build();
    public static final MaterialFlag HAS_SMALL_TFC_ORE = new MaterialFlag.Builder("has_small_tfc_ore").build();
    public static final MaterialFlag HAS_SMALL_NATIVE_TFC_ORE = new MaterialFlag.Builder("has_small_native_tfc_ore").build();
    public static final MaterialFlag GENERATE_DUSTY_ORES = new MaterialFlag.Builder("generate_dusty_ores").build();
}
