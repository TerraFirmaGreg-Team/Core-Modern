package su.terrafirmagreg.core.common.data;

import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("unused")
public class TFGCreativeTab {

    public static void init() {
    }

    public static RegistryEntry<CreativeModeTab> TFG = REGISTRATE.defaultCreativeTab("tfg",
            builder -> builder.title(Component.translatable("tfg.creative_tab.tfg"))
                    .icon(() -> new ItemStack(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get()))
                    .displayItems(new RegistrateDisplayItemsGenerator("tfg", REGISTRATE)))
            .register();

    public record RegistrateDisplayItemsGenerator(String name,
            GTRegistrate registrate) implements CreativeModeTab.DisplayItemsGenerator {

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                CreativeModeTab.Output output) {
            var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : registrate.getAll(Registries.BLOCK)) {
                Block block = entry.get();
                var stack = new ItemStack(block, 1);

                if (registrate.isInCreativeTab(entry, tab))
                    continue;
                if (entry.getId().getNamespace().equals("tfg") && !stack.isEmpty())
                    output.accept(block);
            }
            for (var entry : registrate.getAll(Registries.ITEM)) {
                if (registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                var stack = new ItemStack(item, 1);
                if (item instanceof BlockItem)
                    continue;
                if (entry.getId().getNamespace().equals("tfg"))
                    output.accept(stack);
            }
        }
    }
}
