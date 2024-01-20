package su.terrafirmagreg.core.compat.gtceu;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import su.terrafirmagreg.core.TerraFirmaGreg;
import su.terrafirmagreg.core.compat.gtceu.properties.TFCProperty;

import java.util.Arrays;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.CERTUS;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.ROUGH;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static su.terrafirmagreg.core.compat.gtceu.TFGMaterialFlags.*;

public class TFGMaterials {

    public static Material Latex;
    public static Material Fluix;

    public static Material Gabbro;
    public static Material Shale;
    public static Material Claystone;
    public static Material Limestone;
    public static Material Conglomerate;
    public static Material Dolomite;
    public static Material Chert;
    public static Material Chalk;
    public static Material Rhyolite;
    public static Material Dacite;
    public static Material Slate;
    public static Material Phyllite;
    public static Material Schist;
    public static Material Gneiss;

    public static Material Unknown;
    public static Material PigIron;
    public static Material HighCarbonSteel;
    public static Material HighCarbonBlackSteel;
    public static Material HighCarbonRedSteel;
    public static Material HighCarbonBlueSteel;
    public static Material WeakSteel;
    public static Material WeakRedSteel;
    public static Material WeakBlueSteel;

    public static Material Limonite;
    public static Material Bismuthinite;

    public static void init() {

        /* Specific Materials */

        Latex = new Material.Builder(GTCEu.id("latex"))
                .fluid()
                .color(0xFBB982)
                .buildAndRegister();

        Fluix = new Material.Builder(GTCEu.id("fluix"))
                .fluid()
                .gem(1)
                .color(0xD2D2E6).iconSet(CERTUS)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, FORCE_GENERATE_BLOCK)
                .components(Silicon, 1, Oxygen, 2)
                .color(0x57448d)
                .buildAndRegister();

        /* TFC Stone Types Materials */

        Gabbro = registerOreMaterial(Rock.GABBRO, 0x7F8081);
        Shale = registerOreMaterial(Rock.SHALE, 0x686567);
        Claystone = registerOreMaterial(Rock.CLAYSTONE, 0xAF9377);
        Limestone = registerOreMaterial(Rock.LIMESTONE, 0xA09885);
        Conglomerate = registerOreMaterial(Rock.CONGLOMERATE, 0xA3977F);
        Dolomite = registerOreMaterial(Rock.DOLOMITE, 0x515155);
        Chert = registerOreMaterial(Rock.CHERT, 0x7A6756);
        Chalk = registerOreMaterial(Rock.CHALK, 0xA4A39F);
        Rhyolite = registerOreMaterial(Rock.RHYOLITE, 0x726D69);
        Dacite = registerOreMaterial(Rock.DACITE, 0x979797);
        Slate = registerOreMaterial(Rock.SLATE, 0x989287);
        Phyllite = registerOreMaterial(Rock.PHYLLITE, 0x706B61);
        Schist = registerOreMaterial(Rock.SCHIST, 0x6E735C);
        Gneiss = registerOreMaterial(Rock.GNEISS, 0x6A6D60);

        Unknown = new Material.Builder(GTCEu.id("unknown"))
                .ingot()
                .fluid()
                .color(0x2F2B27)
                .buildAndRegister();

        PigIron = new Material.Builder(GTCEu.id("pig_iron"))
                .ingot()
                .fluid()
                .color(0x6A595C)
                .buildAndRegister();

        HighCarbonSteel = new Material.Builder(GTCEu.id("high_carbon_steel"))
                .ingot()
                .fluid()
                .color(0x5F5F5F)
                .buildAndRegister();
        HighCarbonBlackSteel = new Material.Builder(GTCEu.id("high_carbon_black_steel"))
                .ingot()
                .fluid()
                .color(0x111111)
                .buildAndRegister();

        HighCarbonRedSteel = new Material.Builder(GTCEu.id("high_carbon_red_steel"))
                .ingot()
                .fluid()
                .color(0x700503)
                .buildAndRegister();

        HighCarbonBlueSteel = new Material.Builder(GTCEu.id("high_carbon_blue_steel"))
                .ingot()
                .fluid()
                .color(0x2D5596)
                .buildAndRegister();

        WeakSteel = new Material.Builder(GTCEu.id("weak_steel"))
                .ingot()
                .fluid()
                .color(0x111111)
                .buildAndRegister();

        WeakRedSteel = new Material.Builder(GTCEu.id("weak_red_steel"))
                .ingot()
                .fluid()
                .color(0x700503)
                .buildAndRegister();

        WeakBlueSteel = new Material.Builder(GTCEu.id("weak_blue_steel"))
                .ingot()
                .fluid()
                .color(0x2D5596)
                .buildAndRegister();

        /* Dead */
        Limonite = new Material.Builder(GTCEu.id("limonite")).buildAndRegister();
        Bismuthinite = new Material.Builder(GTCEu.id("bismuthinite")).buildAndRegister();
    }

    public static void postInit() {
        /* TFC Проперти для материалов */
        Copper.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(648, 864, 1080, 1));
        BismuthBronze.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(591, 788, 985, 2));
        Bronze.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(570, 760, 950, 2));
        BlackBronze.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(642, 856, 1070, 2));
        WroughtIron.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3));
        Steel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 4));
        BlackSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(891, 1188, 1485, 5));
        BlueSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 6));
        RedSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 6));

        PigIron.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, 3));
        HighCarbonSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 3));
        HighCarbonBlackSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 5));
        HighCarbonRedSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 5));
        HighCarbonBlueSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 5));
        WeakSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 4));
        WeakBlueSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 5));
        WeakRedSteel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(924, 1232, 1540, 5));
        Unknown.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(240, 320, 400, 1));

        Gold.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(636, 848, 1060, 1));
        Bismuth.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(162, 216, 270, 1));
        Brass.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(558, 744, 930, 2));
        Nickel.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(872, 1162, 1453, 1));
        RoseGold.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(576, 768, 960, 1));
        Silver.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(577, 769, 961, 1));
        Tin.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 230, 1));
        Zinc.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(252, 336, 420, 1));
        SterlingSilver.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(570, 760, 950, 1));
        Iron.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, 3));

        Hematite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 90));
        YellowLimonite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 90));
        Magnetite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 90));
        Pyrite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 90));
        Goethite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 85));
        Malachite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 1080, Copper, 1, 90));
        Tetrahedrite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 1080, Copper, 1, 90));
        Chalcopyrite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 1080, Copper, 1, 90));
        Cassiterite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(648, 864, 230, Tin, 1, 200));
        CassiteriteSand.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 230, Tin, 1, 150));
        Sphalerite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 420, Zinc, 1, 90));
        Garnierite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(138, 184, 1453, Nickel, 1, 90));

        Redstone.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(240, 320, 460, 1));
        RedAlloy.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(570, 650, 740, 2));
        TinAlloy.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(1000, 1100, 1250, 3));

        Bismuthinite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(162, 216, 270, Bismuth, 1));
        Limonite.setProperty(TFGPropertyKeys.TFC_PROPERTY, new TFCProperty(921, 1228, 1535, Iron, 3, 90));

        /* Игнорирование для некоторых металлов */
        var list = Arrays.asList(PigIron, HighCarbonSteel, HighCarbonBlackSteel, HighCarbonRedSteel, HighCarbonBlueSteel, WeakSteel, WeakBlueSteel, WeakRedSteel, Unknown);

        for (var item : list) {
            nugget.setIgnored(item);
            dustTiny.setIgnored(item);
            dustSmall.setIgnored(item);
            dust.setIgnored(item);
            block.setIgnored(item);
        }

        ingot.setIgnored(PigIron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.PIG_IRON).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(HighCarbonSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.HIGH_CARBON_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(HighCarbonBlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.HIGH_CARBON_BLACK_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(HighCarbonRedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.HIGH_CARBON_RED_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(HighCarbonBlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.HIGH_CARBON_BLUE_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(WeakSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WEAK_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(WeakBlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WEAK_BLUE_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(WeakRedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WEAK_RED_STEEL).get(Metal.ItemType.INGOT).get());
        ingot.setIgnored(Unknown, () -> TFCItems.METAL_ITEMS.get(Metal.Default.UNKNOWN).get(Metal.ItemType.INGOT).get());

        TFGTagPrefixes.toolHeadPropick.setIgnored(Copper, () -> TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(BismuthBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(Bronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BRONZE).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(BlackBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_BRONZE).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(WroughtIron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WROUGHT_IRON).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(Steel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.STEEL).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.PROPICK_HEAD).get());
        TFGTagPrefixes.toolHeadPropick.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.PROPICK_HEAD).get());

        TFGTagPrefixes.toolHeadJavelin.setIgnored(Copper, () -> TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(BismuthBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(Bronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BRONZE).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(BlackBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_BRONZE).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(WroughtIron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WROUGHT_IRON).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(Steel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.STEEL).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.JAVELIN_HEAD).get());
        TFGTagPrefixes.toolHeadJavelin.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.JAVELIN_HEAD).get());

        TFGTagPrefixes.toolHeadChisel.setIgnored(Copper, () -> TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(BismuthBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(Bronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BRONZE).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(BlackBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_BRONZE).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(WroughtIron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WROUGHT_IRON).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(Steel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.STEEL).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.CHISEL_HEAD).get());
        TFGTagPrefixes.toolHeadChisel.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.CHISEL_HEAD).get());

        TFGTagPrefixes.toolHeadMace.setIgnored(Copper, () -> TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(BismuthBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(Bronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BRONZE).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(BlackBronze, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_BRONZE).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(WroughtIron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.WROUGHT_IRON).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(Steel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.STEEL).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.MACE_HEAD).get());
        TFGTagPrefixes.toolHeadMace.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.MACE_HEAD).get());

        /* Имеют двойные слитки */
        Gold.addFlags(GENERATE_DOUBLE_INGOTS);
        Bismuth.addFlags(GENERATE_DOUBLE_INGOTS);
        Brass.addFlags(GENERATE_DOUBLE_INGOTS);
        Nickel.addFlags(GENERATE_DOUBLE_INGOTS);
        RoseGold.addFlags(GENERATE_DOUBLE_INGOTS);
        Silver.addFlags(GENERATE_DOUBLE_INGOTS);
        Tin.addFlags(GENERATE_DOUBLE_INGOTS);
        Zinc.addFlags(GENERATE_DOUBLE_INGOTS);
        SterlingSilver.addFlags(GENERATE_DOUBLE_INGOTS);

        /* Имеют инструменты, броню TFC, двойные слитки */
        Copper.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY, CAN_BE_UNMOLDED);
        BismuthBronze.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY, CAN_BE_UNMOLDED);
        Bronze.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY, CAN_BE_UNMOLDED);
        BlackBronze.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY, CAN_BE_UNMOLDED);
        WroughtIron.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY);
        Steel.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY);
        BlackSteel.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY);
        RedSteel.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY);
        BlueSteel.addFlags(GENERATE_DOUBLE_INGOTS, HAS_TFC_TOOL, HAS_TFC_ARMOR, HAS_TFC_UTILITY);

        /* Имеют маленькие куски руды TFC */
        Bismuthinite.addFlags(HAS_SMALL_TFC_ORE);
        Cassiterite.addFlags(HAS_SMALL_TFC_ORE);
        Garnierite.addFlags(HAS_SMALL_TFC_ORE);
        Hematite.addFlags(HAS_SMALL_TFC_ORE);
        Limonite.addFlags(HAS_SMALL_TFC_ORE);
        Magnetite.addFlags(HAS_SMALL_TFC_ORE);
        Malachite.addFlags(HAS_SMALL_TFC_ORE);
        Sphalerite.addFlags(HAS_SMALL_TFC_ORE);
        Tetrahedrite.addFlags(HAS_SMALL_TFC_ORE);

        /* Имеют маленькие чистые куски руды TFC */
        Copper.addFlags(HAS_SMALL_NATIVE_TFC_ORE);
        Gold.addFlags(HAS_SMALL_NATIVE_TFC_ORE);
        Silver.addFlags(HAS_SMALL_NATIVE_TFC_ORE);

        /* Имеют двойные слитки */
        RedAlloy.addFlags(GENERATE_DOUBLE_INGOTS);
        TinAlloy.addFlags(GENERATE_DOUBLE_INGOTS);

        /* Другое */

        // Другое
        Bismuth.setProperty(PropertyKey.ORE, new OreProperty());
        Bismuth.addFlags(EXT2_METAL.toArray(new MaterialFlag[0]));

        Borax.setProperty(PropertyKey.ORE, new OreProperty());

        CertusQuartz.addFlags(GENERATE_ROD);
        NetherQuartz.addFlags(GENERATE_ROD);

        Nickel.addFlags(GENERATE_ROD, GENERATE_LONG_ROD);
        BlackSteel.addFlags(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW);
        BlueSteel.addFlags(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW);
        RedSteel.addFlags(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW);
        WroughtIron.addFlags(GENERATE_ROTOR);

        Copper.addFlags(GENERATE_BOLT_SCREW);
        DamascusSteel.addFlags(GENERATE_BOLT_SCREW);
        Duranium.addFlags(GENERATE_BOLT_SCREW);

        Stone.setProperty(PropertyKey.TOOL, ToolProperty.Builder.of(1.2F, 1.0F, 8, 1, new GTToolType[]{
                GTToolType.AXE,
                GTToolType.HARD_HAMMER,
                GTToolType.HOE,
                GTToolType.KNIFE,
                GTToolType.SHOVEL
        }).build());
        Copper.setProperty(PropertyKey.TOOL, ToolProperty.Builder.of(2.0F, 1.5F, 132, 2).build());
        BismuthBronze.setProperty(PropertyKey.TOOL, ToolProperty.Builder.of(2.5F, 2.0F, 178, 2).build());
        BlackBronze.setProperty(PropertyKey.TOOL, ToolProperty.Builder.of(3.3F, 2.0F, 204, 2).build());
        BlackSteel.setProperty(PropertyKey.TOOL, ToolProperty.Builder.of(6.5F, 4.5F, 1228, 3).build());

        for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            var toolProperty = material.getProperty(PropertyKey.TOOL);
            if (toolProperty == null) continue;

            toolProperty.setDurability(toolProperty.getDurability() * 6);
        }

        block.setIgnored(Fluix, () -> AEBlocks.FLUIX_BLOCK);
        dust.setIgnored(Fluix, () -> AEItems.FLUIX_DUST);
        gem.setIgnored(Fluix, () -> AEItems.FLUIX_CRYSTAL);

        block.setIgnored(CertusQuartz, () -> AEBlocks.QUARTZ_BLOCK);
        dust.setIgnored(CertusQuartz,() -> AEItems.CERTUS_QUARTZ_DUST);
        gem.setIgnored(CertusQuartz, () -> AEItems.CERTUS_QUARTZ_CRYSTAL);

        dust.setIgnored(EnderPearl, () -> AEItems.ENDER_DUST);

        block.modifyMaterialAmount(TFGMaterials.Fluix, GTValues.M * 4);
    }

    private static Material registerOreMaterial(Rock rockType, int color) {
        return new Material.Builder(GTCEu.id(rockType.getSerializedName()))
                .dust()
                .color(color).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, NO_SMASHING, NO_SMELTING)
                .buildAndRegister();
    }


}
