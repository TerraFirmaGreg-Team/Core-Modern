package su.terrafirmagreg.core.common.data;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings("unused")
public class TFGCreativeTab {

    public static void init() {
    }

    public static RegistryEntry<CreativeModeTab> TFG = REGISTRATE.defaultCreativeTab("tfg",
            builder -> builder.title(Component.translatable("tfg.creative_tab.tfg"))
                    .icon(() -> new ItemStack(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get()))
                    .displayItems(new RegistrateDisplayItemsGenerator("tfg", TFGCore.REGISTRATE)))
            .register();

    public record RegistrateDisplayItemsGenerator(String name,
            GTRegistrate registrate) implements CreativeModeTab.DisplayItemsGenerator {

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                CreativeModeTab.Output output) {
            var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : registrate.getAll(Registries.BLOCK)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                if (entry.getId().getNamespace().equals("tfg"))
                    output.accept(entry.get());
            }
            for (var entry : registrate.getAll(Registries.ITEM)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (entry.getId().getNamespace().equals("tfg"))
                    output.accept(entry.get());
            }
        }
    }
}
