package su.terrafirmagreg.core.compat.gtceu.materials;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.level.block.Blocks;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;
import su.terrafirmagreg.core.compat.gtceu.properties.TFCProperty;
import su.terrafirmagreg.core.utils.TFGModsResolver;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.CERTUS;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.ROUGH;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix.*;
import static su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags.*;

public final class TFGMaterialHandler {

	public static void postInit() {

		// I couldn't get setIgnored() to work with TFC things, so they stay here for now. -Py

		bell.setIgnored(Gold, Blocks.BELL);
		bell.setIgnored(Brass, TFCBlocks.BRASS_BELL);
		bell.setIgnored(Bronze, TFCBlocks.BRONZE_BELL);


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

			var metalBlocks = TFCBlocks.METALS.get(metalType);
			lamp.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.LAMP).get());
			anvil.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.ANVIL).get());
			trapdoor.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.TRAPDOOR).get());
			chain.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.CHAIN).get());
			bars.setIgnored(material, () -> metalBlocks.get(Metal.BlockType.BARS).get());
		});


		metalDict.put(Brass, Metal.Default.BRASS);
		metalDict.put(Gold, Metal.Default.GOLD);
		metalDict.put(Nickel, Metal.Default.NICKEL);
		metalDict.put(RoseGold, Metal.Default.ROSE_GOLD);
		metalDict.put(Silver, Metal.Default.SILVER);
		metalDict.put(Tin, Metal.Default.TIN);
		metalDict.put(SterlingSilver, Metal.Default.STERLING_SILVER);
		metalDict.put(Bismuth, Metal.Default.BISMUTH);
		metalDict.put(Zinc, Metal.Default.ZINC);

		metalDict.forEach((material, metalType) -> {
			blockPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK).get());
			stairPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK_STAIRS).get());
			slabPlated.setIgnored(material, () -> TFCBlocks.METALS.get(metalType).get(Metal.BlockType.BLOCK_SLAB).get());
		});


		oreSmall.setIgnored(Bismuth, () -> TFCBlocks.SMALL_ORES.get(Ore.BISMUTHINITE).get());
		oreSmall.setIgnored(Cassiterite, () -> TFCBlocks.SMALL_ORES.get(Ore.CASSITERITE).get());
		oreSmall.setIgnored(Garnierite, () -> TFCBlocks.SMALL_ORES.get(Ore.GARNIERITE).get());
		oreSmall.setIgnored(Hematite, () -> TFCBlocks.SMALL_ORES.get(Ore.HEMATITE).get());
		oreSmall.setIgnored(YellowLimonite, () -> TFCBlocks.SMALL_ORES.get(Ore.LIMONITE).get());
		oreSmall.setIgnored(Magnetite, () -> TFCBlocks.SMALL_ORES.get(Ore.MAGNETITE).get());
		oreSmall.setIgnored(Malachite, () -> TFCBlocks.SMALL_ORES.get(Ore.MALACHITE).get());
		oreSmall.setIgnored(Sphalerite, () -> TFCBlocks.SMALL_ORES.get(Ore.SPHALERITE).get());
		oreSmall.setIgnored(Tetrahedrite, () -> TFCBlocks.SMALL_ORES.get(Ore.TETRAHEDRITE).get());

		oreSmallNative.setIgnored(Copper, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_COPPER).get());
		oreSmallNative.setIgnored(Gold, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_GOLD).get());
		oreSmallNative.setIgnored(Silver, () -> TFCBlocks.SMALL_ORES.get(Ore.NATIVE_SILVER).get());
	}
}
