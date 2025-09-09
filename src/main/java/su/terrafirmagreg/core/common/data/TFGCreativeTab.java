package su.terrafirmagreg.core.common.data;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.SelfTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class TFGCreativeTab {
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFGCore.MOD_ID);

	public static final RegistryObject<CreativeModeTab> TFG = TABS.register("tfg",
		() -> CreativeModeTab.builder()
				  .title(Component.translatable("tfg.creative_tab.tfg"))
				  .icon(() -> new ItemStack(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get()))
				  .displayItems(TFGCreativeTab::fillTab)
				  .build());

	private static void fillTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output out)
	{
		accept(out, TFGBlocks.LUNAR_CHORUS_PLANT);
		accept(out, TFGBlocks.LUNAR_CHORUS_FLOWER);
		accept(out, TFGBlocks.ELECTROMAGNETIC_ACCELERATOR_BLOCK);
		accept(out, TFGBlocks.SUPERCONDUCTOR_COIL_LARGE_BLOCK);
		accept(out, TFGBlocks.SUPERCONDUCTOR_COIL_SMALL_BLOCK);
		accept(out, TFGBlocks.MACHINE_CASING_ALUMINIUM_PLATED_STEEL);
		accept(out, TFGBlocks.REFLECTOR_BLOCK);

		accept(out, TFGBlocks.MARS_DIRT);
		accept(out, TFGBlocks.MARS_FARMLAND);
		accept(out, TFGBlocks.AMBER_MYCELIUM);
		accept(out, TFGBlocks.RUSTICUS_MYCELIUM);
		accept(out, TFGBlocks.SANGNUM_MYCELIUM);

		accept(out, TFGBlocks.LARGE_NEST_BOX);
		accept(out, TFGBlocks.LARGE_NEST_BOX_WARPED);

		accept(out, TFGItems.GLACIAN_WOOL);
		accept(out, TFGItems.SNIFFER_WOOL);
		accept(out, TFGItems.SNIFFER_EGG);
		accept(out, TFGItems.WRAPTOR_WOOL);
		accept(out, TFGItems.WRAPTOR_EGG);

		accept(out, TFGItems.PIGLIN_DISGUISE);
		accept(out, TFGItems.TROWEL);
        accept(out, TFGItems.EMPTY_DNA_SYRINGE);
        accept(out, TFGItems.CLEAN_DNA_SYRINGE);
        accept(out, TFGItems.DIRTY_DNA_SYRINGE);

		accept(out, TFGItems.MARS_WATER_BUCKET);
		accept(out, TFGItems.RAILGUN_AMMO_SHELL);

		accept(out, TFGItems.MOON_RABBIT_EGG);
		accept(out, TFGItems.GLACIAN_RAM_EGG);
		accept(out, TFGItems.SNIFFER_SPAWN_EGG);
		accept(out, TFGItems.WRAPTOR_SPAWN_EGG);
    
		accept(out, TFGItems.ELECTRIC_EXTENDO_GRIP);
	}

	private static <T extends ItemLike, R extends Supplier<T>, K1, K2> void accept(CreativeModeTab.Output out, Map<K1, Map<K2, R>> map, K1 key1, K2 key2)
	{
		if (map.containsKey(key1) && map.get(key1).containsKey(key2))
		{
			out.accept(map.get(key1).get(key2).get());
		}
	}

	private static <T extends ItemLike, R extends Supplier<T>, K> void accept(CreativeModeTab.Output out, Map<K, R> map, K key)
	{
		if (map.containsKey(key))
		{
			out.accept(map.get(key).get());
		}
	}

	private static <T extends ItemLike, R extends Supplier<T>> void accept(CreativeModeTab.Output out, R reg)
	{
		if (reg.get().asItem() == Items.AIR)
		{
			TerraFirmaCraft.LOGGER.error("BlockItem with no Item added to creative tab: " + reg.get().toString());
			SelfTests.reportExternalError();
			return;
		}
		out.accept(reg.get());
	}
}
