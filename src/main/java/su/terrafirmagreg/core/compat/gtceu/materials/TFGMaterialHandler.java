package su.terrafirmagreg.core.compat.gtceu.materials;

import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.items.FLItems;
import com.eerussianguy.firmalife.common.util.FLMetal;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.negodya1.vintageimprovements.VintageBlocks;
import com.negodya1.vintageimprovements.VintageImprovements;
import com.negodya1.vintageimprovements.VintageItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.therighthon.rnr.common.item.RNRItems;
import electrolyte.greate.registry.GreateMaterials;
import earth.terrarium.adastra.common.registry.ModBlocks;
import earth.terrarium.adastra.common.registry.ModItems;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.common.TFGHelpers;

import java.util.HashMap;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix.*;

public final class TFGMaterialHandler {

	// setIgnored() doesn't work very well in KJS despite what GT docs say, so that code lives here instead

	public static void postInit() {

		// Metal things

		bell.setIgnored(Gold, Blocks.BELL);
		bell.setIgnored(Brass, TFCBlocks.BRASS_BELL);
		bell.setIgnored(Bronze, TFCBlocks.BRONZE_BELL);

		bolt.setIgnored(Copper, FirmacivItems.COPPER_BOLT);

		ingot.setIgnored(Zinc, () -> AllItems.ZINC_INGOT);
		ingot.setIgnored(Brass, () -> AllItems.BRASS_INGOT);
		ingot.setIgnored(Vanadium, VintageImprovements.VANADIUM_INGOT);
		ingot.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.INGOT).get());
		ingot.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.INGOT).get());
		ingot.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.INGOT).get());

		ingotDouble.setIgnored(Iron, () -> TFCItems.METAL_ITEMS.get(Metal.Default.CAST_IRON).get(Metal.ItemType.DOUBLE_INGOT).get());
		ingotDouble.setIgnored(BlackSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.DOUBLE_INGOT).get());
		ingotDouble.setIgnored(RedSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.RED_STEEL).get(Metal.ItemType.DOUBLE_INGOT).get());
		ingotDouble.setIgnored(BlueSteel, () -> TFCItems.METAL_ITEMS.get(Metal.Default.BLUE_STEEL).get(Metal.ItemType.DOUBLE_INGOT).get());

		nugget.setIgnored(Zinc, () -> AllItems.ZINC_NUGGET);
		nugget.setIgnored(Copper, () -> AllItems.COPPER_NUGGET);
		nugget.setIgnored(Brass, () -> AllItems.BRASS_NUGGET);
		nugget.setIgnored(Vanadium, VintageImprovements.VANADIUM_NUGGET);

		plate.setIgnored(Copper, () -> AllItems.COPPER_SHEET);
		plate.setIgnored(Brass, () -> AllItems.BRASS_SHEET);
		plate.setIgnored(Gold, () -> AllItems.GOLDEN_SHEET);
		plate.setIgnored(Iron, () -> VintageItems.CAST_IRON_SHEET);
		plate.setIgnored(Cobalt, () -> VintageItems.COBALT_SHEET);
		plate.setIgnored(RoseGold, () -> VintageItems.ROSE_GOLD_SHEET);
		plate.setIgnored(Aluminium, () -> VintageItems.ALUMINUM_SHEET);
		plate.setIgnored(Invar, () -> VintageItems.INVAR_SHEET);
		plate.setIgnored(Lead, () -> VintageItems.LEAD_SHEET);
		plate.setIgnored(Nickel, () -> VintageItems.NICKEL_SHEET);
		plate.setIgnored(Osmium, () -> VintageItems.OSMIUM_SHEET);
		plate.setIgnored(Palladium, () -> VintageItems.PALLADIUM_SHEET);
		plate.setIgnored(Platinum, () -> VintageItems.PLATINUM_SHEET);
		plate.setIgnored(Rhodium, () -> VintageItems.RHODIUM_SHEET);
		plate.setIgnored(Silver, () -> VintageItems.SILVER_SHEET);
		plate.setIgnored(Vanadium, VintageImprovements.VANADIUM_SHEET);
		plate.setIgnored(Zinc, VintageImprovements.ZINC_SHEET);

		block.setIgnored(Vanadium, () -> VintageBlocks.VANADIUM_BLOCK);
		block.setIgnored(Zinc, () -> AllBlocks.ZINC_BLOCK);
		block.setIgnored(Brass, () -> AllBlocks.BRASS_BLOCK);
		block.setIgnored(Steel, ModBlocks.STEEL_BLOCK);

		crushedPurified.setIgnored(Gold, () -> AllItems.CRUSHED_GOLD);
		crushedPurified.setIgnored(Copper, () -> AllItems.CRUSHED_COPPER);
		crushedPurified.setIgnored(Zinc, () -> AllItems.CRUSHED_ZINC);
		crushedPurified.setIgnored(Silver, () -> AllItems.CRUSHED_SILVER);
		crushedPurified.setIgnored(Tin, () -> AllItems.CRUSHED_TIN);
		crushedPurified.setIgnored(Lead, () -> AllItems.CRUSHED_LEAD);

		// Misc

		block.setIgnored(Stone, Blocks.STONE);

		// Create materials

		gem.setIgnored(GreateMaterials.RoseQuartz, () -> AllItems.ROSE_QUARTZ);
		block.setIgnored(GreateMaterials.RoseQuartz, () -> AllBlocks.ROSE_QUARTZ_BLOCK);

		// Ad astra materials

		var desh = TFGHelpers.getMaterial("desh");
		if (desh != null) {
			rawOre.setIgnored(desh, ModItems.RAW_DESH);
			rawOreBlock.setIgnored(desh, ModItems.RAW_DESH_BLOCK);
			block.setIgnored(desh, ModItems.DESH_BLOCK);
			ingot.setIgnored(desh, ModItems.DESH_INGOT);
			nugget.setIgnored(desh, ModItems.DESH_NUGGET);
			plate.setIgnored(desh, ModItems.DESH_PLATE);
		}

		var ostrum = TFGHelpers.getMaterial("ostrum");
		if (ostrum != null) {
			rawOre.setIgnored(ostrum, ModItems.RAW_OSTRUM);
			rawOreBlock.setIgnored(ostrum, ModItems.RAW_OSTRUM_BLOCK);
			block.setIgnored(ostrum, ModItems.OSTRUM_BLOCK);
			ingot.setIgnored(ostrum, ModItems.OSTRUM_INGOT);
			nugget.setIgnored(ostrum, ModItems.OSTRUM_NUGGET);
			plate.setIgnored(ostrum, ModItems.OSTRUM_PLATE);
		}

		var calorite = TFGHelpers.getMaterial("calorite");
		if (calorite != null) {
			rawOre.setIgnored(calorite, ModItems.RAW_CALORITE);
			rawOreBlock.setIgnored(calorite, ModItems.RAW_CALORITE_BLOCK);
			block.setIgnored(calorite, ModItems.CALORITE_BLOCK);
			ingot.setIgnored(calorite, ModItems.CALORITE_INGOT);
			nugget.setIgnored(calorite, ModItems.CALORITE_NUGGET);
			plate.setIgnored(calorite, ModItems.CALORITE_PLATE);
		}

		var etrium = TFGHelpers.getMaterial("etrium");
		if (etrium != null) {
			block.setIgnored(etrium, ModItems.ETRIUM_BLOCK);
			ingot.setIgnored(etrium, ModItems.ETRIUM_INGOT);
			nugget.setIgnored(etrium, ModItems.ETRIUM_NUGGET);
			plate.setIgnored(etrium, ModItems.ETRIUM_PLATE);
			rod.setIgnored(etrium, ModItems.ETRIUM_ROD);
		}

		// Tool-only metals

		var metalDict = new HashMap<Material, Metal.Default>();
		metalDict.put(Copper, Metal.Default.COPPER);
		metalDict.put(BismuthBronze, Metal.Default.BISMUTH_BRONZE);
		metalDict.put(Bronze, Metal.Default.BRONZE);
		metalDict.put(BlackBronze, Metal.Default.BLACK_BRONZE);
		metalDict.put(WroughtIron, Metal.Default.WROUGHT_IRON);
		metalDict.put(Steel, Metal.Default.STEEL);
		metalDict.put(BlackSteel, Metal.Default.BLACK_STEEL);
		metalDict.put(RedSteel, Metal.Default.RED_STEEL);
		metalDict.put(BlueSteel, Metal.Default.BLUE_STEEL);

		metalDict.forEach((material, metalType) -> {
			var metalItems = TFCItems.METAL_ITEMS.get(metalType);
			toolHeadPropick.setIgnored(material, () -> metalItems.get(Metal.ItemType.PROPICK_HEAD).get());
			toolHeadJavelin.setIgnored(material, () -> metalItems.get(Metal.ItemType.JAVELIN_HEAD).get());
			toolHeadChisel.setIgnored(material, () -> metalItems.get(Metal.ItemType.CHISEL_HEAD).get());
			toolHeadMace.setIgnored(material, () -> metalItems.get(Metal.ItemType.MACE_HEAD).get());
			lampUnfinished.setIgnored(material, () -> metalItems.get(Metal.ItemType.UNFINISHED_LAMP).get());

			toolHeadMattock.setIgnored(material, () -> RNRItems.MATTOCK_HEADS.get(metalType).get());
			toolHeadHook.setIgnored(material, () -> metalItems.get(Metal.ItemType.FISH_HOOK).get());

			var metalBlocks = TFCBlocks.METALS.get(metalType);
			lamp.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.LAMP).get());
			anvil.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.ANVIL).get());
			trapdoor.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.TRAPDOOR).get());
			chain.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.CHAIN).get());
			bars.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.BARS).get());
		});

		// All metals

		metalDict.put(Brass, Metal.Default.BRASS);
		metalDict.put(Gold, Metal.Default.GOLD);
		metalDict.put(Nickel, Metal.Default.NICKEL);
		metalDict.put(RoseGold, Metal.Default.ROSE_GOLD);
		metalDict.put(Silver, Metal.Default.SILVER);
		metalDict.put(Tin, Metal.Default.TIN);
		metalDict.put(SterlingSilver, Metal.Default.STERLING_SILVER);
		metalDict.put(Bismuth, Metal.Default.BISMUTH);
		metalDict.put(Zinc, Metal.Default.ZINC);
		metalDict.put(Iron, Metal.Default.CAST_IRON);

		metalDict.forEach((material, metalType) -> {
			blockPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK).get());
			stairPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK_STAIRS).get());
			slabPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK_SLAB).get());
		});

		blockPlated.setIgnored(Chromium, () -> FLBlocks.METALS.get(FLMetal.CHROMIUM).get(Metal.BlockType.BLOCK).get());
		stairPlated.setIgnored(Chromium, () -> FLBlocks.METALS.get(FLMetal.CHROMIUM).get(Metal.BlockType.BLOCK_STAIRS).get());
		slabPlated.setIgnored(Chromium, () -> FLBlocks.METALS.get(FLMetal.CHROMIUM).get(Metal.BlockType.BLOCK_SLAB).get());

		blockPlated.setIgnored(StainlessSteel, () -> FLBlocks.METALS.get(FLMetal.STAINLESS_STEEL).get(Metal.BlockType.BLOCK).get());
		stairPlated.setIgnored(StainlessSteel, () -> FLBlocks.METALS.get(FLMetal.STAINLESS_STEEL).get(Metal.BlockType.BLOCK_STAIRS).get());
		slabPlated.setIgnored(StainlessSteel, () -> FLBlocks.METALS.get(FLMetal.STAINLESS_STEEL).get(Metal.BlockType.BLOCK_SLAB).get());

		// Use TFC ores when they have rich/normal/poor items already

		var oreDict = new HashMap<Material, Ore>();
		oreDict.put(Tetrahedrite, Ore.TETRAHEDRITE);
		oreDict.put(Copper, Ore.NATIVE_COPPER);
		oreDict.put(Gold, Ore.NATIVE_GOLD);
		oreDict.put(Hematite, Ore.HEMATITE);
		oreDict.put(Sphalerite, Ore.SPHALERITE);
		oreDict.put(YellowLimonite, Ore.LIMONITE);
		oreDict.put(Magnetite, Ore.MAGNETITE);
		oreDict.put(Malachite, Ore.MALACHITE);
		oreDict.put(Garnierite, Ore.GARNIERITE);
		oreDict.put(Bismuth, Ore.BISMUTHINITE);
		oreDict.put(Cassiterite, Ore.CASSITERITE);
		oreDict.put(Silver, Ore.NATIVE_SILVER);

		oreDict.forEach((material, ore) -> {
			poorRawOre.setIgnored(material, () -> TFCItems.GRADED_ORES.get(ore).get(Ore.Grade.POOR).get());
			rawOre.setIgnored(material, () -> TFCItems.GRADED_ORES.get(ore).get(Ore.Grade.NORMAL).get());
			richRawOre.setIgnored(material, () -> TFCItems.GRADED_ORES.get(ore).get(Ore.Grade.RICH).get());
		});

		poorRawOre.setIgnored(Chromite, () -> FLItems.CHROMIUM_ORES.get(Ore.Grade.POOR).get());
		rawOre.setIgnored(Chromite, () -> FLItems.CHROMIUM_ORES.get(Ore.Grade.NORMAL).get());
		richRawOre.setIgnored(Chromite, () -> FLItems.CHROMIUM_ORES.get(Ore.Grade.RICH).get());

		oreSmall.setIgnored(Bismuth, () -> TFCBlocks.SMALL_ORES.get(Ore.BISMUTHINITE).get());
		oreSmall.setIgnored(Cassiterite, () -> TFCBlocks.SMALL_ORES.get(Ore.CASSITERITE).get());
		oreSmall.setIgnored(Garnierite, () -> TFCBlocks.SMALL_ORES.get(Ore.GARNIERITE).get());
		oreSmall.setIgnored(Hematite, () -> TFCBlocks.SMALL_ORES.get(Ore.HEMATITE).get());
		oreSmall.setIgnored(YellowLimonite, () -> TFCBlocks.SMALL_ORES.get(Ore.LIMONITE).get());
		oreSmall.setIgnored(Magnetite, () -> TFCBlocks.SMALL_ORES.get(Ore.MAGNETITE).get());
		oreSmall.setIgnored(Malachite, () -> TFCBlocks.SMALL_ORES.get(Ore.MALACHITE).get());
		oreSmall.setIgnored(Sphalerite, () -> TFCBlocks.SMALL_ORES.get(Ore.SPHALERITE).get());
		oreSmall.setIgnored(Tetrahedrite, () -> TFCBlocks.SMALL_ORES.get(Ore.TETRAHEDRITE).get());
		oreSmall.setIgnored(Chromite, FLBlocks.SMALL_CHROMITE);

		oreSmallNative.setIgnored(Copper, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_COPPER).get());
		oreSmallNative.setIgnored(Gold, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_GOLD).get());
		oreSmallNative.setIgnored(Silver, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_SILVER).get());
	}
}
