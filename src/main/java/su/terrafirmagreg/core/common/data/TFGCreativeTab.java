package su.terrafirmagreg.core.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings("unused")
public class TFGCreativeTab {
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFGCore.MOD_ID);

	public static final RegistryObject<CreativeModeTab> TFG = TABS.register("tfg",
		() -> CreativeModeTab.builder()
				  .title(Component.translatable("tfg.creative_tab.tfg"))
				  .displayItems(TFGCreativeTab::fillTab)
				  .build());

	private static void fillTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output out)
	{
		for (var entry : TFGBlocks.BLOCKS.getEntries()) {
			out.accept(entry.get());
		}
	}
}
