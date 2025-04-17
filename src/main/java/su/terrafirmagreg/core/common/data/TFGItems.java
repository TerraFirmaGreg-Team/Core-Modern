package su.terrafirmagreg.core.common.data;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Uncomment TFGCreativeTab in TFGCore if you register anything new here
 */

@SuppressWarnings("unused")
public class TFGItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFGCore.MOD_ID);

	//public static final RegistryObject<Item> EXAMPLE_ITEM = register("pyritie");


	private static RegistryObject<Item> register(String name)
	{
		return register(name, () -> new Item(new Item.Properties()));
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item)
	{
		return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
	}
}
