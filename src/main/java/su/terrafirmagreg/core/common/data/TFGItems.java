package su.terrafirmagreg.core.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.item.ProspectorScannerBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.items.ElectricExtendoGrip;
import su.terrafirmagreg.core.common.data.items.PiglinDisguise;
import su.terrafirmagreg.core.common.data.items.TrowelItem;

import java.util.Locale;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;

/**
 * Uncomment TFGCreativeTab in TFGCore if you register anything new here
 */

@SuppressWarnings("unused")
public class TFGItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFGCore.MOD_ID);

	//public static final RegistryObject<Item> EXAMPLE_ITEM = register("pyritie");
	//public static final GTRegistrate REGISTRATE = GTRegistrate.create(TFGCore.MOD_ID);

	public static final RegistryObject<Item> PIGLIN_DISGUISE =
			ITEMS.register("piglin_disguise", () -> new PiglinDisguise(TFGBlocks.PIGLIN_DISGUISE_BLOCK.get(), new Item.Properties()));

	public static final RegistryObject<Item> TROWEL =
			ITEMS.register("trowel", () -> new TrowelItem(new Item.Properties()));

	public static final RegistryObject<Item> MOON_RABBIT_EGG = registerSpawnEgg(TFGEntities.MOON_RABBIT, 15767516, 9756658);

	@SuppressWarnings("deprecation")
	public static final RegistryObject<BucketItem> MARS_WATER_BUCKET = register("semiheavy_ammoniacal_water_bucket",
		() -> new BucketItem(TFGFluids.MARS_WATER.getSource(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

	public static final RegistryObject<Item> RAILGUN_AMMO_SHELL = ITEMS.register("railgun_ammo_shell", () -> new Item(new Item.Properties().stacksTo(16)));

	public static final RegistryObject<ComponentItem> ELECTRIC_EXTENDO_GRIP = ITEMS.register("electric_extendo_grip", () -> new ElectricExtendoGrip(new Item.Properties()));



	private static RegistryObject<Item> register(String name)
	{
		return register(name, () -> new Item(new Item.Properties()));
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item)
	{
		return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
	}

	private static <T extends EntityType<? extends Mob>> RegistryObject<Item> registerSpawnEgg(RegistryObject<T> entity, int color1, int color2)
	{
		return register("spawn_egg/" + entity.getId().getPath(), () -> new ForgeSpawnEggItem(entity, color1, color2, new Item.Properties()));
	}


}
